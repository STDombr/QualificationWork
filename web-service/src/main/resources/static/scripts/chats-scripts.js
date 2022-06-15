var stompClient = null;

function connect() {
    const socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/queue/private-chatResponse', function (message) {
            newChatMessage(JSON.parse(message.body));
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

        if (chat.classList.contains('active') && chat.id === 'chat' + chatIterator) {
            return;
        }

        if (chat.classList.contains('active')) {
            chat.classList.remove('active', 'text-white');
            chat.classList.add('list-group-item-light');
        } else if (chat.id === 'chat' + chatIterator) {
            chat.classList.remove('list-group-item-light');
            chat.classList.add('active', 'text-white');
        }
    }
    updateMessages();
    $("#chatMessages").html("");
}

function updateMessages() {
    var chats = document.getElementsByName("chat");

    for (var i = 0; i < chats.length; i++) {
        var chat = chats[i];

        if (chat.classList.contains('active')) {
            stompClient.send("/app/private-getAllResponses", {}, JSON.stringify({'chatId': chat.id, 'name': document.getElementById("username").textContent}));
        }

    }
}

function newChatMessage(message) {

    var chat = document.getElementById("chat" + message.chatId).getElementsByClassName("lastMessage")[0];
    chat.innerText = message.body;

    if (message.senderId === document.getElementById("userId").textContent) {

        $("#chatMessages").append(
            '<div class="media w-50 ml-auto mb-3">' +
                '<div class="media-body">' +
                    '<div class="bg-primary rounded py-2 px-3 mb-2">' +
                        '<p class="text-small mb-0 text-white">' +
                            message.body +
                       '</p>' +
                    '</div>' +
                    '<p class="small text-muted">' +
                        message.time +
                    '</p>' +
                '</div>' +
            '</div>');

    } else {

        $("#chatMessages").append(
            '<div class="media w-50 mb-3">' +
                '<div class="media-body ml-3">' +
                    '<div class="bg-light rounded py-2 px-3 mb-2">' +
                        '<p class="text-small mb-0 text-muted">' +
                            message.body +
                        '</p>' +
                        '</div>' +
                            '<p class="small text-muted">' +
                        message.time +
                    '</p>' +
                '</div>' +
            '</div>');

    }

}

function sendMessage(element) {
    if (event.key === 'Enter' && element.value !== "") {
        console.log("Send message: ", element.value);

        var chats = document.getElementsByName("chat");

        for (var i = 0; i < chats.length; i++) {
            var chat = chats[i];

            if (chat.classList.contains('active')) {
                stompClient.send("/app/private-sendMessage", {}, JSON.stringify({'chatId': chat.id, 'username': document.getElementById("username").textContent, 'recipientId': chat.getElementsByClassName("recipientId")[0].innerText,'body': element.value}));
            }

        }
        element.value = "";
    }
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    connect();
});