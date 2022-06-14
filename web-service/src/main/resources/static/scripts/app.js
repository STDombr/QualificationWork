var stompClient = null;
var cancel = null;
var seconds = null;
var timer = null;
var modal = null;

/*function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#serverStart").prop("disabled", !connected);
    $("#serverPause").prop("disabled", !connected);
    $("#serverDisconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}*/

function connect() {
    const socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('topic/chatInfo', function (message) {
            console.log("WWWTTTFFF");
            newChatInfo(JSON.parse(message.body));
            console.log("WWWTTTFFF");
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
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
        stompClient.send("/app/answer", {}, JSON.stringify({'id': document.getElementById("messageId").textContent, 'option': 'Yes'}));
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
        stompClient.send("/app/answer", {}, JSON.stringify({'id': document.getElementById("messageId").textContent, 'option': 'No'}));
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
    console.log("tessssttt");
    $("#modal-body").append(
        "<div>" +
        "Opponent for question '" + message + "' found" +
        "</div>");
    console.log("tessssttt");
    modal.show();
    console.log("tessssttt");
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
});