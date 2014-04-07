/* Manage messages between server and clients */

var Logger = {
    threshold:2,
    info:function(message){
        if(this.threshold >= 2){
            console.log("INFO",message);
        }
    },error:function(message){
        if(this.threshold >= 1){
            console.log("ERROR",message);
        }
    }
}


var	GameConnection = {
    baseUrl:'http://localhost:8081/tichu-server/rest',
    loadGame:function(name){
        $.ajax({
            url:this.baseUrl + '/game/info/' + name,
            dataType:'jsonp',
            success:function(data){
                Table.display(data);
            }
        });
    },
    /* Create connection to a party */
    connect:function(game,player){
        console.log(this.url)
        $.ajax({
            url:this.baseUrl + '/game/join',
            data:{game:game,name:player},
            dataType:'jsonp',
            success:function(data){
               if(data.status!=null && data.status == 0){
                    Logger.error(data.message);
                    alert(data.message);
                }else{
                    WebSocketManager.init(data.token,player);
                }
            },error:function(a,b,c){
                console.log("ERROR",a,b,c);
            }
        })
    }
}

var WebSocketManager = {
    token:null,	// token give by server to communicate
    player:null,
    ws:null,
    url:"ws://localhost:8081/tichu-server/chat4",
    init:function(token,player){
        if(token == null){
          Logger.error("No token defined");
            return;
        }
        this.player = player;
        this.token = token;
        this.ws = new WebSocket(this.url + "?token=" + this.token);
        this.ws.onmessage = function(message){
            Logger.info(message);
            WebSocketManager.readMessage(message.data);
        }
        this.ws.onopen = function(message){
            Logger.info("Open connexion");
        }
    },
    readMessage:function(message){
        try{
            var data = JSON.parse(message);
            MessageDispatcher.dispatch(data);
        }catch(e){
            Logger.error("NOT JSON : " + message + e);
        }
    },
    sendMessage:function(message){
        this.ws.send(message);
    },
    close:function(){
        this.ws.close();
    }
}

var SenderManager = {
    changeCards:function(toLeft,toPartner,toRight){
        var cards = {
            toLeft:{value:toLeft.value,color:toLeft.color},
            toPartner:{value:toPartner.value,color:toPartner.color},
            toRight:{value:toRight.value,color:toRight.color}
        }
        var data = {type:'CHANGE_CARDS',value:JSON.stringify(cards)};
        WebSocketManager.sendMessage(JSON.stringify(data));
    }
}

 var MessageDispatcher = {
    dispatch:function(data){
        switch(data.type){
            case "CONNECTION_KO" : WebSocketManager.close();break;
            case "CONNECTION_OK" :
                Table.connectPlayer(data.object.orientation,true);
                Logger.info("Well Connected");
            break;
            case "PLAYER_DISCONNECTED" : Table.disconnectPlayer(data.object.orientation);break;
            case "PLAYER_SEATED" : Table.connectPlayer(data.object.orientation,false);break;
            case "DISTRIBUTION" : Table.distribute(data.object); break;
            case "CHANGE_CARD_MODE":Table.behaviours.changeMode.enable();break;
            case "CARDS_CHANGED":Table.behaviours.changeMode.disable();break;
            case "NEW_CARDS":Table.receiveCards(data.object);break;
            case "NEXT_PLAYER":Table.nextPlayer(data.object);break;
        }
    }
 }
     /*
  CONNECTION_OK
  CONNECTION_KO
  PLAYER_DISCONNECTED
  PLAYER_SEATED
GAME_MODEDISTRIBUTION
CHANGE_CARD_MODE
CARDS_CHANGED
NEW_CARDS
FOLD_PLAYED
BOMB_PLAYED
CALL_PLAYED
NOT_YOUR_TURN
NO_CALL_WHEN_FIRST
BAD_FOLD
NEXT_PLAYER
TURN_WIN
PLAYER_END_ROUND
SCORE
GAME_WIN
*/