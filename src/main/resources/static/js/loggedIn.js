let selectedUser = null;
const messages = {};
var socket = new SockJS('http://localhost:2422/ws-presence');
var stompClient = Stomp.over(socket);

function selectUser(element) {
    const items = document.querySelectorAll('.contact-item');
    items.forEach(item => item.classList.remove('selected'));

    element.classList.add('selected');

    selectedUser = element.getAttribute('data-jid');

    document.getElementById('recipientJid').value = selectedUser;

    loadMessages(selectedUser);

    const notificationIcon = element.querySelector('.notification-icon');
    if (notificationIcon) {
        notificationIcon.remove();
    }

    const isGroup = element.querySelector('.contact-status').textContent === 'Grupo';
    if (isGroup) {
        console.log('Grupo seleccionado:', selectedUser);
    } else {
        console.log('Usuario seleccionado:', selectedUser);
    }
}

function joinGroup(groupName) {
    fetch('/joinGroup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
            'groupName': groupName
        }),
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('Unido al grupo:', groupName);
            addGroupToList(groupName);
            subscribeToGroupMessages(groupName);
            document.getElementById('searchGroup').value = '';
        } else {
            console.error('Error al unirse al grupo:', data.message);
        }
    })
    .catch(error => {
        console.error('Error al unirse al grupo:', error);
    });
}

function addGroupToList(groupName) {
    const contactList = document.getElementById('contactList');
    // Verificar si el grupo ya está en la lista
    if (!document.querySelector(`[data-jid="${groupName}"]`)) {
        const listItem = document.createElement('li');
        listItem.classList.add('contact-item');
        listItem.setAttribute('data-jid', groupName);
        listItem.innerHTML = `
            <span>${groupName}</span>
            <div class="contact-status">Grupo</div>
        `;
        listItem.onclick = function() {
            selectUser(this);
        };
        contactList.appendChild(listItem);
    }
}

function loadUserGroups() {
    fetch('/getUserGroups')
    .then(response => response.json())
    .then(groups => {
        groups.forEach(group => {
            addGroupToList(group);
            subscribeToGroupMessages(group);
        });
    })
    .catch(error => {
        console.error('Error al cargar los grupos del usuario:', error);
    });
}

function updateContactList(presenceData) {
    const contactList = document.getElementById('contactList');
    if (!contactList) {
        console.error('Elemento #contactList no encontrado');
        return;
    }

    const existingContacts = {};
    contactList.querySelectorAll('.contact-item').forEach(item => {
        const jid = item.getAttribute('data-jid');
        existingContacts[jid] = true;
    });

    contactList.innerHTML = ''; 

    for (const [user, details] of Object.entries(presenceData)) {
        const listItem = document.createElement('li');
        listItem.classList.add('contact-item');
        listItem.setAttribute('data-jid', user);

        listItem.innerHTML = `
            <span>${user}</span>
            <div class="contact-status">${details.mode || 'Desconocido'}</div>
            <div class="contact-status-detail">${details.status || ''}</div>
        `;

        // Añadir el evento onclick para el nuevo elemento
        listItem.onclick = function() {
            selectUser(this);
        };

        contactList.appendChild(listItem);
        existingContacts[user] = false; // Marcamos como procesado
    }

    Object.keys(existingContacts).forEach(jid => {
        if (existingContacts[jid]) {
            
        }
    });

    console.log('Lista de contactos actualizada:', contactList.innerHTML);
}


function loadMessages(sender) {
    const chat = document.getElementById('chat');
    chat.innerHTML = '';  

    if (messages[sender]) {
        messages[sender].forEach(message => {
            const messageElement = document.createElement('div');
            messageElement.classList.add('message');

            if (message.fromUser) {
                messageElement.classList.add('from-user');
            }

            if (message.additionalClasses && message.additionalClasses.length > 0) {
                const validClasses = message.additionalClasses.filter(cls => cls.trim() !== '');
                messageElement.classList.add(...validClasses);
            }

            messageElement.innerHTML = `
                <p>${message.time}, ${message.date}</p>
                <div class="message-content">${message.text}</div>
            `;
            chat.appendChild(messageElement);
        });

        chat.scrollTop = chat.scrollHeight; 
    }
}

function showNotificationIcon(sender) {
    const userElement = document.querySelector(`[data-jid="${sender}"]`);
    if (userElement) {
        let notificationIcon = userElement.querySelector('.notification-icon');
        if (!notificationIcon) {
            // Crear el ícono si no existe
            notificationIcon = document.createElement('img');
            notificationIcon.src = './images/notificacion.png';  
            notificationIcon.classList.add('notification-icon');
            notificationIcon.alt = 'Nuevo mensaje';
            userElement.appendChild(notificationIcon);
        }
    }
}

function playNotificationSound() {
    const notificationSound = document.getElementById('notificationSound');
    if (notificationSound) {
        notificationSound.play().catch((error) => {
            console.error('Error al reproducir el sonido de notificación:', error);
        });
    }
}

function subscribeToGroupMessages(groupName) {
    stompClient.subscribe(`/topic/groupMessages/${groupName}`, function(message) {
        const groupMessage = JSON.parse(message.body);
        addMessageToChat(groupName, `${groupMessage.sender}: ${groupMessage.content}`);
    });
}

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    loadUserGroups();
    stompClient.subscribe('/topic/presenceUpdates', function (presenceMessage) {
        try {
            var message = JSON.parse(presenceMessage.body);
            console.log("Received presence update:", message);
            updateContactList(message);
        } catch (error) {
            console.error('Error parsing message:', error);
        }
    });

    stompClient.subscribe('/topic/myPresenceUpdates', function (myPresenceMessage) {
        try {
            var message = JSON.parse(myPresenceMessage.body);
            console.log("Received my presence update:", message);

            handleMyPresenceUpdate(message);

        } catch (error) {
            console.error('Error parsing message:', error);
        }
    });

    stompClient.subscribe('/topic/messageUpdates', function (message) {
        try {
            const msgData = JSON.parse(message.body);
    
            for (const [sender, messagesArray] of Object.entries(msgData)) {
                if (!messages[sender]) {
                    messages[sender] = [];
                }

                messagesArray.forEach((messageText) => {
                    const isDuplicate = messages[sender].some(msg => msg.text === messageText && !msg.fromUser);
                    if (!isDuplicate) {
                        messages[sender].push({
                            text: messageText,
                            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
                            date: new Date().toLocaleDateString([], { weekday: 'short', day: '2-digit', month: 'short' }),
                            fromUser: false
                        });

                        if (selectedUser !== sender) {
                            showNotificationIcon(sender);
                            playNotificationSound();
                        }
                    }
                });

                if (selectedUser === sender) {
                    loadMessages(sender);
                }
                
            }
        } catch (error) {
            console.error('Error al procesar el mensaje:', error);
        }
    });
    
}, function (error) {
    console.error('STOMP error:', error);
});

function handleMyPresenceUpdate(presenceData) {
    console.log('Mi presencia inicial:', presenceData);
}

function addMessageToChat(sender, messageText) {
    const chat = document.getElementById('chat');
    
    const message = document.createElement('div');
    message.classList.add('message');
    
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const date = new Date().toLocaleDateString([], { weekday: 'short', day: '2-digit', month: 'short' });

    message.innerHTML = `<p>${time}, ${date}</p><div class="message-content">${messageText}</div>`;
    
    chat.appendChild(message);
    chat.scrollTop = chat.scrollHeight;  

    // Guarda el mensaje en el objeto messages
    if (!messages[sender]) {
        messages[sender] = [];
    }
    messages[sender].push({
        text: messageText,
        time: time,
        date: date,
        fromUser: false
    });
}

// Función para actualizar la lista de contactos
/*function updateContactList(presenceData) {
    const contactList = document.getElementById('contactList');
    if (!contactList) {
        console.error('Elemento #contactList no encontrado');
        return;
    }

    contactList.innerHTML = ''; // Limpiar la lista existente

    for (const [user, details] of Object.entries(presenceData)) {
        const listItem = document.createElement('li');
        listItem.classList.add('contact-item');
        listItem.setAttribute('data-jid', user);
        
        // Agregar detalles del usuario y presencia
        listItem.innerHTML = `
            <span>${user}</span>
            <div class="contact-status">${details.mode || 'Desconocido'}</div>
            <div class="contact-status-detail">${details.status || ''}</div>
        `;

        // Añadir el evento onclick para el nuevo elemento
        listItem.onclick = function() {
            selectUser(this);
        };
        
        contactList.appendChild(listItem);
    }

    console.log('Lista de contactos actualizada:', contactList.innerHTML);
}*/

document.addEventListener('DOMContentLoaded', (event) => {

    const statusSelector = document.getElementById('statusSelector');
    const statusInput = document.querySelector('.status_input');

    statusSelector.addEventListener('change', function() {
        const selectedStatus = statusSelector.value; 
        console.log('Nuevo estado seleccionado:', selectedStatus);
        
        updatePresence(selectedStatus);
    });

    if (statusInput) {
        statusInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();

                const status = statusInput.value;
                const selectedStatus = statusSelector.value;

                updatePresence(selectedStatus, status);
            }
        });
    }
    
    function updatePresence(mode, status) {
        status = statusInput ? statusInput.value : '';
        status = status;
        const presenceData = {
            status: status,
            mode: mode 
        };
    
        stompClient.send('/app/myPresence', {}, JSON.stringify(presenceData));
        stompClient.send('/topic/myPresenceUpdates', {}, JSON.stringify(presenceData));
        
        const presenceDisplay = document.getElementById('presenceDisplay');
        if (presenceDisplay) {
            presenceDisplay.textContent = mode;
        }
    }

    if (Notification.permission !== 'granted') {
        Notification.requestPermission();
    }

    const sendButton = document.getElementById('sendButton');
    const messageInput = document.getElementById('messageInput');

    if (sendButton && messageInput) {
        sendButton.addEventListener('click', function(event) {
            event.preventDefault();

            const messageText = messageInput.value;

            if (messageText.trim() !== '' && selectedUser) {
                console.log('Enviando mensaje:', messageText);

                fetch('/send', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: new URLSearchParams({
                        'recipientJid': selectedUser,
                        'messageText': messageText,
                    }),
                })
                .then(response => response.text())
                .then(data => {
                    console.log('Respuesta del servidor:', data);

                    const chat = document.getElementById('chat');
                    
                    const message = document.createElement('div');
                    message.classList.add('message', 'from-user');
                    
                    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                    const date = new Date().toLocaleDateString([], { weekday: 'short', day: '2-digit', month: 'short' });

                    message.innerHTML = `<p>${time}, ${date}</p><div class="message-content">${messageText}</div>`;
                    
                    chat.appendChild(message);
                    chat.scrollTop = chat.scrollHeight; 

                    if (!messages[selectedUser]) {
                        messages[selectedUser] = [];
                    }
                    messages[selectedUser].push({
                        text: messageText,
                        time: time,
                        date: date,
                        fromUser: true
                    });

                    messageInput.value = '';
                })
                .catch(error => {
                    console.error('Error al enviar el mensaje:', error);
                });
            }
        });
    }
});