/* Manage messages between server and clients */
function endsWith(value,suffix){
    return value.lastIndexOf(suffix) == value.length - suffix.length;
}

var BASE_URL = location.protocol + "//" + location.host + location.pathname + (endsWith(location.pathname,"/")?"":"/");
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

var GameInfo = {
   baseUrl: BASE_URL + 'rest',
   loadGame:function(name,callback){
       $.ajax({
           url:this.baseUrl + '/game/info/' + name,
           dataType:'jsonp',
           success:function(data){
               callback(data);
           }
       });
   },
   listGames:function(callback){
        $.ajax({
            url:this.baseUrl + '/game/listFree',
            dataType:'jsonp' ,
            success:function(data){
                var list = [];
                for(var nb in data){
                    data[nb].forEach(function(game){
                        list.push({game:game,nb:4-nb});
                    });
                }
                list.sort(function(a,b){
                    return b.nb - a.nb;
                });
                callback(list);
            }
        })
   },
   delete:function(name,callback){
    $.ajax({
        url:this.baseUrl + '/game/delete/' + name,
        dataType:'json',
        success:function(data){
         if(data.status == 1){
            callback();
         }else{
            MessageInfo.fail("Impossible to delete " + name);
         }
        }
    });
   }
}

var	GameConnection = {
    baseUrl: BASE_URL + 'rest',
    /* Create connection to a party */
    /* @param rename : set if user want to change connection name */
    connect:function(game,player,rename,success,error){
        var data = {game:game,name:player};
        if(rename && rename != player){
            data.renameData = rename;
        }
        $.ajax({
            url:this.baseUrl + '/game/join',
            data:data,
            dataType:'jsonp',
            success:function(data){
               if(data.status!=null && data.status == 0){
                    Logger.error(data.message);
                    if(error){
                        error(data.message);
                    }
                }else{
                    WebSocketManager.init(data.token,player);
                    if(success){
                        success();
                    }
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
    url:BASE_URL.replace("http","ws") + 'chat4',
    init:function(token,player){
        if(token == null){
          Logger.error("No token defined");
            return;
        }
        this.player = player;
        this.token = token;
        try{
            this.ws = new WebSocket(this.url + "?token=" + this.token);
            this.ws.onmessage = function(message){
                Logger.info(message);
                WebSocketManager.readMessage(message.data);
            }
            this.ws.onopen = function(message){
                Logger.info("Open connexion");
                Chat.connect(WebSocketManager.token);
                WebSocketManager.heartbeat.start();
            },
            this.ws.onclose = function(){
                Logger.error("CONNECTION CLOSE");
                this.close();
                WebSocketManager.ws = null;
                WebSocketManager.heartbeat.stop();
                // Try to reconnect
                WebSocketManager.reconnect();
            },
            this.ws.onerror = function(e){
                Logger.error("Connection error " + e);
            }
        }catch(e){
            Logger.error("Error with game websocket " + e);
        }
    },
    /* When server close connection, try to reconnect */
    reconnect:function(){
       Logger.error("TRY RECONNECT...");
       // Wait 1/2 s
       // 2 solutions : get delta events or load everything (reset screen)
       setTimeout(function(){
           WebSocketManager.init(WebSocketManager.token,WebSocketManager.player);
       },1000);
    },
    heartbeat:{
      process:null,
      start:function(){
        this.process = setInterval(function(){
            WebSocketManager.sendMessage("{\"type\":\"HEARTBEAT\"}");
        },500);
      },
      stop:function(){
        clearInterval(this.process);
        this.process = null;
      }
    },
    stopHeartBeat:function(){
        clearInterval(this.heartbeatProcess);
        this.heartbeatProcess = null;
    },
    sendHeartBeat:function(){
        this.ws.send()
    },

    readMessage:function(message){
        try{
            var data = JSON.parse(message);
            var task = function(){MessageDispatcher.dispatch(data);};
            //Fifo.exec(data);
            Fifo.exec(task);
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
    playBomb:function(fold){
        this._send('BOMB',fold);
    },
    callGame:function(){
        this._send('CALL','')
    },
    sendChangePlayer:function(player){
        this._send('DRAGON_CHOICE',player);
    },
    _send:function(type,object){
        var data = typeof object == "object" ? JSON.stringify(object) : object;
        this._sendJSON(type,data);
    },
    _sendJSON:function(type,json){
        var data = {type:type,value:json};
        WebSocketManager.sendMessage(JSON.stringify(data));
    }
}

 var MessageDispatcher = {
    countdown:null,
    init:function(){
        this.countdown = new CountDown(function(){
            Fifo.run();
        });
        ComponentManager.add(this.countdown);
    },
    dispatch:function(data){
        switch(data.type){
            case "CONNECTION_KO" : WebSocketManager.close();break;
            case "CONNECTION_OK" :
                Table.connectPlayer(data.object.playerUser.orientation,true);
                Table.display(data.object);
            break;
            case "CHEATER" : Table.showCheater(data.object);break;
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
            case "GAME_MODE":MessageInfo.info("Let's go");break;
            case "NOT_YOUR_TURN":MessageInfo.fail("Not your turn, stop it");break;
            case "FOLD_PLAYED":PlayerManager.playFold(data.object);break;
            case "BOMB_PLAYED":PlayerManager.playBomb(data.object);break;
            case "CALL_PLAYED":PlayerManager.call(data.object);break;
            case "NO_CALL_WHEN_FIRST":MessageInfo.fail("Have to play a card");break;
            case "WHERE_GIVE_FOLD":Table.selectPlayerToGiveFold();break;
            case "GIVE_FOLD_DRAGON":Table.giveFoldForDragon(data.object);break;
            case "BAD_FOLD":Table.showBadFold(data.object);break;
            case "CAPOT":MessageInfo.info("Capot");break;
            case "TURN_WIN":
                this.temporize(4000,function(){PlayerManager.winTurn(data.object);});
                break;
            case "ROUND_WIN":MessageInfo.info("Player " + data.object.name + " finish first");break;
            case "PLAYER_END_ROUND":PlayerManager.endRound(data.object);break;
            case "PLAYER_ANNONCE":Table.playerDoAnnonce(data.object,data.object.annonce);break;
            case "ANNONCE_FORBIDDEN":MessageInfo.fail("Annonce forbidden " + data.object);break;
            case "SCORE":
                Scorer.addResult(data.object);
                this.temporize(4000);
                break;
            case "GAME_WIN":
                this.temporize(6000);
                break;
        }
    },
    /* Tezmporize execution of behave. Bufferize instructions */
    temporize:function(during,fct){
       Fifo.stop();
       if(fct){
        Fifo.exec(fct);
       }
       this.countdown.start(during);
    }
 }

var Fifo = {
    buffer : new Array(),
    pause : false,

    /* Exec immediately or put in fifo execution */
    exec : function(task){
        if(!this.pause && this.buffer.length == 0){
            this._execTask(task);
        }else{
            this._add(task);
        }
    },

    _execTask : function(task){
        if(task == null){return;}
        task();
        //MessageDispatcher.dispatch(task);
    },
    run:function(){
        this.pause = false;
        this.start();
    },
    // Stop running tasks, put in fifo
    stop : function(){
        this.pause = true;
    }  ,

    start : function(){
        while(this.buffer.length>0 && this.pause == false){
            this._execTask(this._get());
        }
    },

    _add : function(task){
        this.buffer.push(task);
    },

    _get : function(){
        var value = this.buffer.splice(0,1);
        if(value.length == 0){
            return null;
        }
        return value[0];
    }
}

MessageDispatcher.init();