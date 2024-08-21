let selectedUser = null;
const messages = {};
var socket = new SockJS('http://localhost:2422/ws-presence');
var stompClient = Stomp.over(socket);

function selectUser(element) {
    // Eliminar la clase 'selected' de todos los elementos de la lista
    const items = document.querySelectorAll('.contact-item');
    items.forEach(item => item.classList.remove('selected'));
    
    // Añadir la clase 'selected' al elemento clicado
    element.classList.add('selected');

    // Obtener el JID del usuario seleccionado
    selectedUser = element.getAttribute('data-jid');

    // Imprimir el JID del usuario seleccionado en la consola
    console.log('Usuario seleccionado:', selectedUser);

    // Configurar el campo oculto con el JID del destinatario
    document.getElementById('recipientJid').value = selectedUser;

    // Cargar mensajes del usuario seleccionado
    loadMessages(selectedUser);
}

function loadMessages(sender) {
    const chat = document.getElementById('chat');
    chat.innerHTML = '';  // Limpiar el chat antes de cargar mensajes

    if (messages[sender]) {
        messages[sender].forEach(message => {
            const messageElement = document.createElement('div');
            messageElement.classList.add('message');

            // Aplicar la clase 'from-user' si el mensaje fue enviado por el usuario actual
            if (message.fromUser) {
                messageElement.classList.add('from-user');
            }

            // Si tienes más clases para agregar, asegúrate de que no estén vacías
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

        chat.scrollTop = chat.scrollHeight;  // Scroll al final del chat
    }
}



// Este bloque de código se ejecuta cuando el WebSocket recibe un mensaje
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/presenceUpdates', function (presenceMessage) {
        try {
            var message = JSON.parse(presenceMessage.body);
            console.log("Received presence update:", message);
            updateContactList(message);
        } catch (error) {
            console.error('Error parsing message:', error);
        }
    });

    stompClient.subscribe('/topic/messageUpdates', function (message) {
        try {
            const msgData = JSON.parse(message.body);
    
            // Itera sobre cada usuario en los datos recibidos
            for (const [sender, messagesArray] of Object.entries(msgData)) {
                // Verifica si el mensaje es del usuario actualmente seleccionado
                if (selectedUser === sender) {
                    // Obtén el último mensaje del usuario seleccionado
                    const latestMessage = messagesArray[messagesArray.length - 1];
                    
                    // Agrega el mensaje al chat solo si pertenece al usuario seleccionado
                    addMessageToChat(sender, latestMessage);
                }
            }
        } catch (error) {
            console.error('Error al procesar el mensaje:', error);
        }
    });
    
    
}, function (error) {
    console.error('STOMP error:', error);
});

function addMessageToChat(sender, messageText) {
    const chat = document.getElementById('chat');
    
    const message = document.createElement('div');
    message.classList.add('message');
    
    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    const date = new Date().toLocaleDateString([], { weekday: 'short', day: '2-digit', month: 'short' });

    message.innerHTML = `<p>${time}, ${date}</p><div class="message-content">${messageText}</div>`;
    
    chat.appendChild(message);
    chat.scrollTop = chat.scrollHeight;  // Scroll to the bottom of the chat

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
function updateContactList(presenceData) {
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
}

// Este bloque de código se ejecuta cuando la página está cargada
document.addEventListener('DOMContentLoaded', (event) => {
    const sendButton = document.getElementById('sendButton');
    const messageInput = document.getElementById('messageInput');

    if (sendButton && messageInput) {
        sendButton.addEventListener('click', function(event) {
            event.preventDefault(); // Evitar el comportamiento predeterminado del formulario

            const messageText = messageInput.value;

            if (messageText.trim() !== '' && selectedUser) {
                // Imprimir el mensaje en la consola
                console.log('Enviando mensaje:', messageText);

                // Enviar el mensaje al servidor
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

                    // Agregar el mensaje al chat
                    const chat = document.getElementById('chat');
                    
                    const message = document.createElement('div');
                    message.classList.add('message', 'from-user');
                    
                    const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
                    const date = new Date().toLocaleDateString([], { weekday: 'short', day: '2-digit', month: 'short' });

                    message.innerHTML = `<p>${time}, ${date}</p><div class="message-content">${messageText}</div>`;
                    
                    chat.appendChild(message);
                    chat.scrollTop = chat.scrollHeight;  // Scroll to the bottom of the chat

                    // Guardar el mensaje en el objeto messages
                    if (!messages[selectedUser]) {
                        messages[selectedUser] = [];
                    }
                    messages[selectedUser].push({
                        text: messageText,
                        time: time,
                        date: date,
                        fromUser: true
                    });

                    // Limpiar el campo de entrada
                    messageInput.value = '';
                })
                .catch(error => {
                    console.error('Error al enviar el mensaje:', error);
                });
            }
        });
    }
});
