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
            Chat.connect(WebSocketManager.token);
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
        this._send('CHANGE_CARDS',cards);
    },
    annonceGrandTichu:function(){
        this._sendJSON('ANNONCE','GRAND_TICHU')
    },
    annonceTichu:function(){
        this._send('ANNONCE','TICHU')
    },
    showLastCards:function(){
        this._send('SUITE_CARDS','');
    },
    sendCards:function(fold){
        this._send('FOLD',fold);
    },
    callGame:function(){
        this._send('CALL','')
    },
    _send:function(type,object){
        this._sendJSON(type,JSON.stringify(object));
    },
    _sendJSON:function(type,json){
        var data = {type:type,value:json};
        WebSocketManager.sendMessage(JSON.stringify(data));
    }
}

 var MessageDispatcher = {
    dispatch:function(data){
        switch(data.type){
            case "CONNECTION_KO" : WebSocketManager.close();break;
            case "CONNECTION_OK" :
                Table.connectPlayer(data.object.playerUser.orientation,true);
                Table.display(data.object);
            break;
            case "PLAYER_DISCONNECTED" : Table.disconnectPlayer(data.object.orientation);break;
            case "PLAYER_SEATED" : Table.connectPlayer(data.object.orientation,false);break;
            case "DISTRIBUTION" : Table.distribute(data.object); break;
            case "DISTRIBUTION_PART1" : Table.distributeFirstPart(data.object); break;
            case "DISTRIBUTION_PART2" : Table.distributeSecondPart(data.object); break;
            case "SEE_ALL_CARDS" : Table.notifyPlayerSeeCards(data.object); break;
            case "CHANGE_CARD_MODE":Table.behaviours.changeMode.enable();break;
            case "CARDS_CHANGED":Table.behaviours.changeMode.disable();break;
            case "NEW_CARDS":Table.receiveCards(data.object);break;
            case "NEXT_PLAYER":PlayerManager.nextPlayer(data.object);break;
            case "GAME_MODE":break;
            case "NOT_YOUR_TURN":alert("Not your turn, stop it");break;
            case "FOLD_PLAYED":PlayerManager.playFold(data.object);break;
            case "CALL_PLAYED":console.log("CALL");break;
            case "NO_CALL_WHEN_FIRST":alert("Have to play a card");break;
            case "BAD_FOLD":alert("Bad fold");Table.cancelLastFold();break;
            case "TURN_WIN":PlayerManager.winTurn(data.object);break;
            case "PLAYER_END_ROUND":PlayerManager.endTurn(data.object);break
            case "PLAYER_ANNONCE":Table.playerDoAnnonce(data.object,data.object.annonce);break
            case "ANNONCE_FORBIDDEN":alert("Annonce forbidden " + data.object);break
            case "SCORE":Scorer.addResult(data.object);break
            case "GAME_WIN":alert("End of the game");break
        }
    }
 }
     /*

BOMB_PLAYED


GAME_WIN
*/