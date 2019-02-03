var id = undefined;
var hosturl = 'http://localhost:8090';
$(document).ready(function() {
    var cookieid = Cookies.get('clipboard.id');
    id = cookieid;
    if (cookieid === undefined) {
        window.location.href = hosturl + '/index.html'
    } else {
        showData(cookieid);
    }
});

function showData(cookieid) {
    $.ajax({
        url: hosturl + '/get-data?id=' + cookieid
    }).then(function(data) {
        $('#content-container').text(data.stringData);
        //$('#content-container').append(data.stringData);
        //TODO: copy to clipboard

    });
}

function connect() {
    var socket = new SockJS('/clipboard-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        //TODO subscribe to user specific channel
        if (id === undefined) return;
        stompClient.subscribe('/topic/data-received', function (data) {
            console.log("subscription is called"+data);
            showData(id);
        });
    });
}