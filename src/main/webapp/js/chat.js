/* Manage chez chat between user */

var CHAT_URL = 'ws://localhost:8081/tichu-server/chat';

var Chat = {
    chatWS:null,
    url:CHAT_URL,
    token:null,
    div:null,
    init:function(){
        this.div = $('#idChatDiv');
        $('#idMessage').bind('keydown',function(e){
            if(e.keyCode == 13){
                var message = $(this).val();
                if(ActionParser.check(message)){
                    Chat.send(message);
                    Chat._showMessage({name:'Me'},message);
                }  else{
                    ActionParser.parse(message);
                }
                $(this).val('');
            }
        });
        $('#idMessage').bind('keyup',function(e){
            if(ActionParser.checkAutocomplete($(this).val())){
                console.log("open");
            }
        });
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
    info:function(message){
        this._showMessage({name:"INFO"},message,"green");
    },
    _showMessage:function(player,message,color){
        if(PlayerManager.getPlayerUser().equals(player)){return;}
        color = color || 'black';
        if(this.div){
            this.div.append('<span style="color:' + color + '">' + player.name + " : " + unescape(message) + '</span><br/>');
        }else{
            console.log(player.name,unescape(message));
        }
    },
    send:function(message){
        if(this.chatWS == null){return;}
        this.chatWS.send(escape(message));
    }
}

var ActionParser = {
    call:new RegExp(/\/call$/),
    checkAutocomplete:function(message){
        return message.length == 1 && message == "/";
    },
    check:function(message){
        return message.indexOf('/') == -1;
    },
    parse:function(message){
        if(this.call.test(message)){
            Chat._showMessage({name:'INFO'},'You call the game','blue');
        }
    }
}