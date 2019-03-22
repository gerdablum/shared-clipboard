var id = null;
var hosturl = location.protocol + '//' + location.host;
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
    //Only show the button when automatic clipboard copying failed.
    $('#button').hide();
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
            copyAndAskForPermission(data.stringData);
        // data is of type file
        } else if (data.type === 'FILE') {
            $('#content-container').text('');
            displayFileData(data);
        }
    });
}

function copyAndAskForPermission(text) {
    try {
        navigator.permissions.query({
            name: 'clipboard-write'
        }).then(function(permissionStatus) {
            // Will be 'granted', 'denied' or 'prompt':
            console.log(permissionStatus.state);
            copy(permissionStatus.state, text);
            permissionStatus.onchange = copy(permissionStatus.state, text);
        });

    } catch(err) {
        console.log("asking for permission failed");
        fallbackCopy();
    }
}
function copy(state, text) {
    if (state === 'granted') {
        navigator.clipboard.writeText(text).then(function () {
            console.log('copying successful');
        }).catch(function (err){
            console.log("copying failed ");
            fallbackCopy();
        });
    } else if (state === 'denied'){
        fallbackCopy();
    }
}

function fallbackCopy() {
    var btn = $('#button');
    btn.show();
    btn.click(function() {
        var $temp = $("<input>");
        $("body").append($temp);
        $temp.val($('#content-container').text()).select();
        document.execCommand("copy");
        $temp.remove();
    });
}

function connect() {
    socket = new SockJS('/clipboard-websocket');
    var stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        if (id === null) return;
        stompClient.subscribe('/topic/data-received/' + id, function (data) {
            console.log("subscription is called" + data);
            showData(id);
        });

        stompClient.subscribe('/topic/acknowledge/' + id, function (data) {
            if(data.body == 'logout') {
                document.cookie = "clipboard.id= " + id + "; expires=Thu, 01 Jan 1970 00:00:01 GMT;";
                window.location.href = hosturl;
            } else {
                console.log("acknowledge complete, redirecting to content page.");
                var now = new Date();
                var inTenMinutes = new Date(now.getTime() + 30* 60000);
                document.cookie = "clipboard.id= " + id +"; expires=" + inTenMinutes.toUTCString();
                window.location.href = hosturl + '/display-data.html';
            }

        })
    });
}

function disconnect() {
    if (socket != null) {
        socket.close();
    }
}

function logout() {
    $.ajax({
        url: hosturl + '/logout',
        type: 'get',
        data: {
            id: id
        }
    })
}

function displayFileData(data) {
    var mimeType = data.mimeType;
    var btn = $('#button');
    btn.text('Click to download');
    btn.show();
    if (mimeType.includes('image')) {
        var image = new Image();
        image.src = "data:" + mimeType + ";base64," + data.base64;
        image.alt = "Your clipboard content.";
        image.width = 100;
        $('#content-container').append(image);
    } else {
        //TODO: Escape filename in server!!!!!
        $('#content-container').html(data.originalFileName);
    }
    btn.attr('download', data.originalFileName);
    btn.attr('href', "data:" + mimeType + ";base64," + data.base64);
}