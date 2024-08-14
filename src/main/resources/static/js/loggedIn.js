// Tiempo en milisegundos entre cada actualización (ej. 5000 ms = 5 segundos)
const updateInterval = 5000;

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

                    messageInput.value = '';
                })
                .catch(error => console.error('Error:', error));
            }
        });
    }

    // Comenzar a actualizar las presencias periódicamente
    updatePresences();
    setInterval(updatePresences, updateInterval);
});

function updatePresences() {
    fetch('/presences')
        .then(response => response.json())
        .then(data => {
            console.log('Presencias actualizadas:', data);
            updateContactList(data);
        })
        .catch(error => console.error('Error al obtener presencias:', error));
}

function updateContactList(presences) {
    const contactList = document.getElementById('contactList');
    const items = contactList.querySelectorAll('.contact-item');
    
    items.forEach(item => {
        const jid = item.getAttribute('data-jid');
        if (presences[jid]) {
            item.querySelector('.status').textContent = presences[jid];
        }
    });
}

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
