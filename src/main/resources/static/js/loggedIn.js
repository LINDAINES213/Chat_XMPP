let selectedUser = null;
const messages = {};

function selectUser(element) {
    // Eliminar la clase 'selected' de todos los elementos de la lista
    const items = document.querySelectorAll('.contact-item');
    items.forEach(item => item.classList.remove('selected'));
    
    // Añadir la clase 'selected' al elemento clicado
    element.classList.add('selected');

    // Obtener el JID del usuario seleccionado
    selectedUser = element.getAttribute('data-jid');

    // Cargar mensajes del usuario seleccionado
    loadMessages(selectedUser);
}

function loadMessages(user) {
    const chat = document.getElementById('chat');
    chat.innerHTML = '';

    if (messages[user]) {
        messages[user].forEach(msg => {
            const message = document.createElement('div');
            message.classList.add('message', msg.fromUser ? 'from-user' : '');
            message.innerHTML = `<p>${msg.time}, ${msg.date}</p><div class="message-content">${msg.text}</div>`;
            chat.appendChild(message);
        });
        chat.scrollTop = chat.scrollHeight;  // Scroll to the bottom of the chat
    }
}

document.addEventListener('DOMContentLoaded', (event) => {
    const sendButton = document.getElementById('sendButton');
    const messageInput = document.getElementById('messageInput');

    if (sendButton && messageInput) {
        sendButton.addEventListener('click', function() {
            const messageText = messageInput.value;

            if (messageText.trim() !== '' && selectedUser) {
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
            }
        });
    }
});
