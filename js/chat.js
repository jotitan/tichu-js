/* Manage chez chat between user */

var Chat = {
    chatWS:null,
    url:'ws://localhost:8081/tichu-server/chat',
    token:null,
    connect:function(token){
        this.token = token;
        this.chatWS = new WebSocket(this.url + '?token=' + token);
        this.chatWS.onmessage = function(message){
            var data = JSON.parse(message.data);
            var p = PlayerManager.getByOrientation(message.player);
            console.log(p.name + " : " + data.message);
        }
        this.chatWS.onopen = function(){
            console.log("chat connected");
        }
        this.chatWS.close = function(){
            Chat.chatWS = null;
            Chat.connect(Chat.token);
        }
    },
    send:function(message){
        if(this.chatWS == null){return;}
        this.chatWS.send(message);
    }
}