const socket = new WebSocket('ws://localhost:2422/ws/presences');

socket.onmessage = function(event) {
    const presencesMap = JSON.parse(event.data);
    updatePresenceUI(presencesMap);
};

function updatePresenceUI(presencesMap) {
    // Lógica para actualizar la interfaz de usuario con la nueva información de presencia
    console.log(presencesMap);
}
