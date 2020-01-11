var app = require('express');
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];

server.listen(8080, () => {
    console.log("Server is now running...");
});

io.on('connection', (socket) => {
    console.log("Player Connected!");
    socket.emit('socketID', {id: socket.id});
    socket.emit('getPlayers', players);
    socket.broadcast.emit('newPlayer', {id: socket.id});
    socket.on("playerMoved", (data) => {
        data.id = socket.id;
        socket.broadcast.emit('playerMoved', data);

        // console.log("playerMoved: " + 
        //             "ID: " + data.id +
        //             " X: " + data.x +
        //             " Y: " + data.y +
        //             " Angle: " + data.angle); 

        for (var i = 0; i < players.length; i++){
            if(players[i].id == data.id){
                players[i] = data;
            }
        }
    });
    socket.on('disconnect', () => {
        console.log("Player Disconnected!");
        socket.broadcast.emit('playerDisconnected', {id:socket.id});
        for(var i = 0; i < players.length; i++){
            if (players[i].id == socket.id){
                players.splice(i, 1);
            }
        }
    });
    players.push(new player(socket.id, 0, 0, 0));
});

function player(id, x, y, angle){
    this.id = id;
    this.x = x;
    this.y = y;
    this.angle = angle;
}