var id = null;
var hosturl = 'http://localhost:8090';
var socket = null;

function loadIdAndQRCode() {

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

}

function lookupCookies() {
    var cookieid = Cookies.get('clipboard.id');
    id = cookieid;
    if (cookieid === undefined) {
        window.location.href = hosturl + '/index.html'
    } else {
        showData(cookieid);
        connect();
    }
}

function showData(cookieid) {
    $.ajax({
        url: hosturl + '/get-data?id=' + cookieid
    }).then(function(data) {
        // data is of type string
        if (data.type === 'STRING') {
            $('#content-container').html(data.stringData);
        // data is of type file
        } else if (data.type === 'FILE') {
            $('#content-container').text('');
            displayFileData(data);
        }
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
            var now = new Date();
            var inTenMinutes = new Date(now.getTime() + 30* 60000);
            document.cookie = "clipboard.id= " + id +"; expires=" + inTenMinutes.toUTCString();
            window.location.href = hosturl + '/display-data.html';
        })
    });
}

function disconnect() {
    if (socket != null) {
        socket.close();
    }
}

function displayFileData(data) {
    var mimeType = data.mimeType;
    if (mimeType.includes('image')) {
        var image = new Image();
        image.src = "data:" + mimeType + ";base64," + data.base64;
        image.alt = "Your clipboard content.";
        image.width = "100%";
        $('#content-container').append(image);
    } else {
        var link = $('<a></a>');
        link.attr('download', data.originalFileName);
        link.attr('href', "data:" + mimeType + ";base64," + data.base64);
        link.text("Click here to download " + data.originalFileName);
        $('#content-container').append(link);
    }
}