/* Manage chez chat between user */

var Chat = {
    chatWS:null,
    url:'ws://localhost:8081/tichu-server/chat',
    token:null,
    div:null,
    init:function(){
        this.div = $('#idChatDiv');
        $('#idMessage').bind('keydown',function(e){
            if(e.keyCode == 13){
                Chat.send($(this).val());
                Chat._showMessage({name:'Me'},$(this).val());
                $(this).val('');
            }
        })
    },
    connect:function(token){
        this.token = token;
        this.chatWS = new WebSocket(this.url + '?token=' + token);
        this.chatWS.onmessage = function(message){
            var data = JSON.parse(message.data);
            var p = PlayerManager.getByOrientation(data.player);
            Chat._showMessage(p,data.message);
        }
        this.chatWS.onopen = function(){
            console.log("chat connected");
        }
        this.chatWS.close = function(){
            Chat.chatWS = null;
            Chat.connect(Chat.token);
        }
    },
    _showMessage:function(player,message){
        Chat.div.append(player.name + " : " + message);
    },
    send:function(message){
        if(this.chatWS == null){return;}
        this.chatWS.send(message);
    }
}