var id = null;
var hosturl = 'http://localhost:8090';
var socket = null;
$(document).ready(function() {

    $.ajax({
        url: hosturl + '/get-id'
    }).then(function(data) {
        var url = hosturl + '/qr-image.png?id=' + data.id;
        var i = new Image();
        i.src = url;
        i.alt = 'QR Code zum scannen';
        $('#image-container').append(i);
        $('#text').append(data.id);
        id = data.id;
        connect();
    });

});

function lookupCookies() {
    var cookieid = Cookies.get('clipboard.id');
    id = cookieid;
    if (cookieid === undefined) {
        window.location.href = hosturl + '/index.html'
    } else {
        showData(cookieid);
    }
}

function showData(cookieid) {
    $.ajax({
        url: hosturl + '/get-data?id=' + cookieid
    }).then(function(data) {
        $('#content-container').text(data.stringData);
        //TODO: copy to clipboard

    });
}

function connect() {
    socket = new SockJS('/clipboard-websocket');
    var stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        if (id === null) return;
        stompClient.subscribe('/topic/data-received/' + id, function (data) {
            console.log("subscription is called"+data);
            showData(id);
        });

        stompClient.subscribe('/topic/acknowledge/' + id, function (data) {
            console.log("acknowledge complete, redirecting to content page.");
            //TODO does not work ??
            var now = new Date();
            var inTenMinutes = new Date(now.getTime() + 10* 60000);
            document.cookie = "clipboard.id= " + id +"; expires=" + inTenMinutes.toUTCString();
            window.location.href = hosturl + '/display-data.html';
        })
    });
}

function disconnect() {
    if (stompClient != null) {
        socket.close();
    }
}