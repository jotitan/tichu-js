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
    url:this.baseUrl + '/game/join',
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
        $.ajax({
            url:this.url,
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
            console.log(data)
            MessageDispatcher.dispatch(data);
        }catch(e){
            Logger.error("NOT JSON : " + message);
        }
    },
    sendMessage:function(message){
        this.ws.send(message);
    },
    close:function(){
        this.ws.close();
    }
}

 var MessageDispatcher = {
    dispatch:function(data){
        switch(data.responseType){
            case "CONNECTION_KO" : WebSocketManager.close();break;
            case "CONNECTION_OK" : Logger.info("Well Connected");break;
            case "PLAYER_DISCONNECTED" : Logger.info("Pause");break;
            case "PLAYER_SEATED" : Table.connectPlayer(data.orientation);Logger.info("Show place color");break;
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