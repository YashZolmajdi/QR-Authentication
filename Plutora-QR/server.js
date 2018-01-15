var express = require('express');
var path = require('path');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);

app.use(express.static(__dirname));

app.get('/', function (req, res) {
    return res.sendFile(path.resolve(__dirname + '/Login.html'));
});

app.get('/qr/login/:id/:username', function (req, res) {
    if (io.sockets.sockets[req.params.id] != undefined) {
        io.to(req.params.id).emit('logedIn', req.params.username);
        res.status(202);
        return res.send('OK');
    } else {
        res.status(400);
        return res.send('No connection was found');
    }
});

io.on('connection', function (socket) {
    console.log('a user connected');
});

http.listen(8081, function () {
    console.log('listening on *:3000');
});
