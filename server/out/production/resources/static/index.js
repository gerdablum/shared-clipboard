$(document).ready(function() {

    if (Cookies.get('clipboard.id') != undefined) {
        //redirect to content page
        window.location.href = 'http://localhost:8090/display-data.html'
    }
    $.ajax({
        url: 'http://localhost:8090/get-id'
    }).then(function(data) {
        var url = 'http://localhost:8090/qr-image.png?id=' + data.id;
        var i = new Image();
        i.src = url;
        i.alt = 'QR Code zum scannen';
        $('#image-container').append(i);
        $('#text').append(data.id);
        //TODO does not work
        var now = new Date();
        var inTenMinutes = new Date(now.getTime() + 10* 60000);
        document.cookie = "clipboard.id= " + data.id +"; expires=" + inTenMinutes.toUTCString();
    });
});