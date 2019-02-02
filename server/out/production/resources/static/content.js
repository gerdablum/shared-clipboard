$(document).ready(function() {
    var cookieid = Cookies.get('clipboard.id')
    if (cookieid == undefined) {
        window.location.href = 'http://localhost:8090/index.html'
    }
    $.ajax({
        url: "http://localhost:8090/get-data?id=" + cookieid
    }).then(function(data) {
        $('#content-container').append(data.stringData);
        //TODO: copy to clipboard

    });
});