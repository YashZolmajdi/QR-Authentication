function selectLoginOption(type) {
    $('.login-options .active').removeClass('active');

    if (type == 0) {
        $('#manual-icon').attr('src', 'img/manual.svg');
        $('#qr-icon').attr('src', 'img/qr-code-white.svg');
        $('.manual').show();
        $('.one-step').hide();
        $('#manual').addClass('active');
    } else {
        $('#manual-icon').attr('src', 'img/manual-white.svg');
        $('#qr-icon').attr('src', 'img/qr-code.svg');
        $('.manual').hide();
        $('.one-step').show();
        $('#one-step').addClass('active');
    }

    localStorage.setItem('login-type', type);
}

$(document).ready(function () {
    var socket = io();
    socket.on('connect', () => {
        var url = $(location).attr('protocol')+'//'+$(location).attr('hostname')+':'+$(location).attr('port');
        url = url + '/qr/login/' + socket.id;
        var json = '{"url":"'+url+'"}';
        $('#qrcode').qrcode({width: 200,height: 200,text: json});
    });
    socket.on('logedIn', (token) => {
        $('.qr-code').hide();
        $('.tick').show();
        setTimeout(function(){ 
            $('.left').animate({left:'-'+$('.left').width()}, 1000, "linear");
            $('.right').animate({right:'-'+$('.right').width()}, 1000, "linear");
        }, 2000);
        $('strong').text(token);
    });
    selectLoginOption(localStorage.getItem('login-type'));
});