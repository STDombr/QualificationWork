var stompClient = null;
var cancel = null;
var seconds = null;
var timer = null;
var modal = null;

function connect() {
    const socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/user/queue/private-chatInfo', function (message) {
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

function sendYes() {
    if (stompClient !== null) {
        document.getElementById("buttonYes").disabled = true;
        document.getElementById("buttonNo").disabled = true;
        document.getElementById("waiting").hidden = false;
        if (seconds < 30) {
            seconds = 31;
        }
        stompClient.send("/app/private-answer", {}, JSON.stringify({'id': document.getElementById("messageId").textContent, 'option': 'Yes', 'name': document.getElementById("username").textContent}));
    }
}

function sendNo() {
    if (stompClient !== null) {
        document.getElementById("buttonYes").disabled = true;
        document.getElementById("buttonNo").disabled = true;
        document.getElementById("waiting").hidden = false;
        if (seconds < 30) {
            seconds = 31;
        }
        stompClient.send("/app/private-answer", {}, JSON.stringify({'id': document.getElementById("messageId").textContent, 'option': 'No', 'name': document.getElementById("username").textContent}));
    }
}

function updateTime() {
    if (seconds > 0) {
        seconds -= 1;
        timer.innerText = seconds + "s";
    } else {
        window.location.replace("/questions");
        seconds = document.getElementById("time").textContent;
    }
}

function newChatInfo(message) {
    $("#modal-body").append(
        "<div>" +
        "Opponent for question '" + message.question + "' found" +
        "</div>");
    modal.show();
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    connect();
    modal = new bootstrap.Modal(document.querySelector('#modal'));
    timer = document.getElementById("timer");
    seconds = document.getElementById("time").textContent;
    cancel = setInterval(updateTime, 1000);
    $("#buttonYes").click(function () {
        sendYes();
    });
    $("#buttonNo").click(function () {
        sendNo();
    });
    $("#goToChats").click(function () {
        window.location.replace("/chats");
    });
});