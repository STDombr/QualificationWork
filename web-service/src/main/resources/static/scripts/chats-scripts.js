var stompClient = null;

function connect() {
    const socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/queue/private-chatResponse', function (message) {
            newChatInfo(JSON.parse(message.body));
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function activateChat(chatIterator) {
    var chats = document.getElementsByName("chat");

    for (var i = 0; i < chats.length; i++) {
        var chat = chats[i];

        if (chat.classList.contains('active') && chat.id !== 'chat' + chatIterator) {
            chat.classList.remove('active', 'text-white');
            chat.classList.add('list-group-item-light');
        } else if (chat.id === 'chat' + chatIterator) {
            chat.classList.remove('list-group-item-light');
            chat.classList.add('active', 'text-white');
        }

    }
}

/**function newChatInfo(message) {
    $("#greetings").append(
        "<tr>" +
        "<td>" + message.eventId + "</td>" +
        "<td>" + message.eventName + "</td>" +
        "<td>" + message.callId + "</td>" +
        "<td>" + message.dn + "</td>" +
        "</tr>");
    stompClient.send("/app/respond", {}, JSON.stringify(message));
}**/

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    connect();
});