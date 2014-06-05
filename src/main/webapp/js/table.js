/* Manage the table of game */
/* Behave : build table, show new player (name, connected or not) */

var EventHelper = {
	getPosition:function(e){
		var x = e.clientX - $('#canvas').offset().left;
		var y = e.clientY - $('#canvas').offset().top + $('body').scrollTop();
		return {x:x,y:y};
	}
}

var Table = {
    folds:[],
    cardFolds:[],
    turn:0,		// Turn of game
    doPlayFold:function(cards,player){
        Sorter.cards(cards);
        cards.sort(function(c1,c2){
            if(c1.getValue() != c2.getValue()){
                return c1.getValue() - c2.getValue();
            }
            return c1.color > c2.color ? 1 : c1.color < c2.color ? -1 : 0;
        });
        // If the brower player play, remove the cards of his hand, otherwise, remove hide cards
        cards.forEach(function(c,i){
            c.setStatus(STATUS_CARD.TABLE_CARD);
            c.drawing.recto = false;
            c.drawing.checked = false;
            c.drawing.setOrientation("C",i,player,cards.length);
            c.drawing.deep = this.cardFolds.length;
            if(player.equals(PlayerManager.getPlayerUser())){
                player.playCard(c);
            }   else{
                player.removeEmptyCards(1);
            }
        },this);

        this.cardFolds.push(cards);
        // Reorganize cards
        player.sortCards();
        CardManager.sortCards();
        if(player.equals(PlayerManager.getPlayerUser())){
            Actions.deleteByClass("tichu");
        }
    },
    doPlayCall:function(player){

    },
    /* Display the context */
    display:function(data){
        Actions.empty();
        data.players.forEach(function(p){
            var pl = PlayerManager.getByOrientation(p.orientation);
            pl.setName(p.name);
            if(p.connected){
                pl.drawing.setOnline();
            }else{
                pl.drawing.setOffline();
            }
            if(p.lastFoldIsCall){
                pl.playCall();
            }
            pl.setAnnonce(p.annonce);
            if(!pl.equals(PlayerManager.getPlayerUser())){
                pl.setNecessaryEmptyCards(p.nbCard);
            }else{
                if(!pl.isAnnonce() && p.nbCard == 14){
                    Actions.build("tichu");
                }
            }
        });

        CardManager.resetStatus();
        CombinaisonsValidator.resetTurn();
        PlayerManager.getPlayerUser().removeCards();
        if(data.cards && data.cards.length > 0){
            data.cards.forEach(function(c){
                PlayerManager.getPlayerUser().giveCard(CardManager.get(c.value,c.color));
            });
            PlayerManager.getPlayerUser().sortCards();
            PlayerManager.getPlayerUser().detectBombs();
            PlayerManager.getPlayerUser().displayBombs();
            if(data.type == "NEXT_PLAYER"){
                // Player served, reconnect, he can see all his cards
                PlayerManager.getPlayerUser().served = true;
            }
        }
        // Get the folds
        data.folds.forEach(function(fold){
           PlayerManager.playFold(fold);
        });

        // Load score
        Scorer.newgame();
        if(data.scoreTeam1!=null && data.scoreTeam2!=null && data.scoreTeam1.length == data.scoreTeam2.length){
            for(var i = 0 ; i < data.scoreTeam1.length ; i++){
                scorer.addResult({score1:data.scoreTeam1[i],score2:data.scoreTeam2[i]})
            }
        }

        this._dispatchState(data);
    },
    _dispatchState:function(data){
        switch(data.type){
            case "CHANGE_CARD_MODE":Table.behaviours.changeMode.enable();break;
            case "DISTRIBUTION_PART1":Actions.build('grandTichu','lastCards');break;
            case "NEXT_PLAYER":PlayerManager.nextPlayer(data.currentPlayer);break;
        }
    },
    playerDoAnnonce:function(player,annonce){
        var pl = PlayerManager.getByOrientation(player.orientation);
        pl.setAnnonce(annonce);
    },
    connectPlayer:function(orientation,currentUser){
        PlayerManager.getByOrientation(orientation).connect(currentUser);
    },
    disconnectPlayer:function(orientation,currentUser){
        PlayerManager.getByOrientation(orientation).drawing.setOffline();
    },
    /* When receive the 3 cards of others players */
    receiveCards:function(cards){
        var cl = CardManager.get(cards.toLeft.value,cards.toLeft.color);
        var cp = CardManager.get(cards.toPartner.value,cards.toPartner.color);
        var cr = CardManager.get(cards.toRight.value,cards.toRight.color);

        PlayerManager.getPlayerUser().giveCard(cl);
        PlayerManager.getPlayerUser().giveCard(cp);
        PlayerManager.getPlayerUser().giveCard(cr);
        this.behaviours.changeMode.showChangedCards(cl,cp,cr);
        this.resetTurn();
        PlayerManager.getPlayerUser().detectBombs();
    },
    notifyPlayerSeeCards:function(player){
      var p = PlayerManager.getByOrientation(player.orientation);
      if(!p.equals(PlayerManager.getPlayerUser())){
        p.createEmptyCards(5);
      }
    },
    showGameWinner:function(teamWin){
        MessageInfo.info("End of the game, team " + (teamWin+1) + " win");
        Scorer.showWinner(teamWin);
    },
    resetTurn:function(){
        CombinaisonsValidator.resetTurn();
        this.folds = [];
        this.cardFolds.forEach(function(fold){
            fold.forEach(function(card){
                card.setStatus(STATUS_CARD.PLAYED_CARD);
            });
        });
        this.cardFolds = [];
    },
    playFold:function(fold){
        var player = PlayerManager.getByOrientation(fold.player);
        var cards = fold.cards.map(function(c){return CardManager.get(c.value,c.color);});
        cards.forEach(function(c,i){
            c.setStatus(STATUS_CARD.TABLE_CARD);
            c.drawing.recto = false;
            c.setChecked(false);
            c.drawing.setOrientation("C",i,this.folds.length);
            c.drawing.deep = this.folds.length;
            // If the brower player play, remove the cards of his hand, otherwise, remove hide cards
            if(player.equals(PlayerManager.getPlayerUser())){
                player.playCard(c);
            }else{
                player.removeEmptyCards(1);
            }
        },this);

        this.folds.push(fold.cards);
        player.sortCards();
    },
    /* Call only for the player who play a bad fold */
    cancelLastFold:function(){
        this.folds.splice(this.folds.length -1,1);
        CombinaisonsValidator.removeLast();
    },
    /* First part of card (can make grand tichu) */
    distributeFirstPart:function(cards){
        Actions.build('grandTichu','lastCards');
        PlayerManager.players.forEach(function(p){
           if(p.equals(PlayerManager.getPlayerUser())){
            var user = PlayerManager.getPlayerUser();
            cards.forEach(function(card){
                user.giveCard(CardManager.get(card.value,card.color));
            });
           }else{
                // Players see just the 9 first cards
                p.createEmptyCards(9);
           }
           p.sortCards();
        });
    },
    distributeSecondPart:function(cards){
        Actions.empty();
        var user = PlayerManager.getPlayerUser();
        if(!user.isAnnonce()){
            Actions.build("tichu");
        }
        cards.forEach(function(card){
            user.giveCard(CardManager.get(card.value,card.color));
        });
        user.sortCards();
    },
    behaviours:{
        changeMode:{
            boxes:[],
            isEnabled:false,
            enable:function(){
                if(this.isEnabled){
                    return;
                }
                /* Add box to drop the card */
                this._buildBoxes();
                this.mouseController.enable(this.boxes);
                Actions.build('swapCards');
                this.isEnabled = true;
            },
            _buildBoxes:function(){
                var width = ComponentManager.variables.width/2;
                var height = ComponentManager.variables.height-200;

                this.boxes.push(new BoxCard(width-75,height,35,50));
                this.boxes.push(new BoxCard(width-25,height-20,35,50));
                this.boxes.push(new BoxCard(width+25,height,35,50));
                this.boxes.forEach(function(b){ComponentManager.add(b);});
            },
            validate:function(){
                var check = this.boxes.every(function(b){return b.card!=null;});
                if(!check){
                    alert("impossible, choose one card for each");
                    return;
                }
                Actions.empty();
                this.boxes.forEach(function(b){
                    PlayerManager.getPlayerUser().playCard(b.card.card);
                    //b.card.card.setStatus(STATUS_CARD.CHANGED_CARD);
                });
                SenderManager.changeCards(this.boxes[0].card.card,this.boxes[1].card.card,this.boxes[2].card.card);
            },
            disable:function(){
                this.boxes.forEach(function(b){
                    b.card.card
                    ComponentManager.remove(b);
                });
                PlayerManager.getPlayerUser().sortCards();
                this.mouseController.disable();
                this.isEnabled = false;
            },
            /* Display received cards */
            showChangedCards:function(fromLeft,fromPartner,fromRight){
                this.boxes.forEach(function(b){
                    b.card.card.setStatus(STATUS_CARD.NO_STATUS_CARD);
                    PlayerManager.getPlayerUser().playCard(b.card.card);
                });
                fromLeft.drawing.setDirectCoordinates(this.boxes[0].x+5,this.boxes[0].y+5);
                fromPartner.drawing.setDirectCoordinates(this.boxes[1].x+5,this.boxes[1].y+5);
                fromRight.drawing.setDirectCoordinates(this.boxes[2].x+5,this.boxes[2].y+5);

                Actions.build('acceptCards');
            },
            endChangeCards:function(){
                this.boxes = [];
                PlayerManager.getPlayerUser().sortCards(true);
                Actions.empty();
                PlayerManager.getPlayerUser().displayBombs();
                if(PlayerManager.getPlayerUser().equals(PlayerManager.currentPlayer)){
                    Table.behaviours.gameMode.enable();
                }
            },
            mouseController:{
                moving:false,
                card:null,
                boxes:null,
                disable:function(){
                    this.boxes = null;
                    $('#canvas').unbind('mousemove.swap');
                    $('#canvas').unbind('mouseup.swap');
                    $('#canvas').unbind('mousedown.swap');
                },
                enable:function(boxes){
                    this.boxes = boxes;
                    var _self = this;
                    $('#canvas').bind('mousemove.swap',function(e){_self.move(e);});
                    $('#canvas').bind('mouseup.swap',function(e){_self.up(e);});
                    $('#canvas').bind('mousedown.swap',function(e){_self.down(e);});
                },
                move:function(e){
                    if(!this.moving || !this.card){return;}
                    var coords = this.getPosition(e);
                    this.card.setDirectCoordinates(coords.x-10,coords.y-10);
                },
                down:function(e){
                    var coords = this.getPosition(e);
                    var box = this._findBox(coords);
                    if(box !=null){
                        box.card = null;
                    }
                    var card = CardManager.findSelectCards(PlayerManager.getPlayerUser(),coords.x,coords.y);
                    if(card == null){return;}
                    this.card = card;
                    this.card.originPos = card.originPos || card.getCoords();
                    this.moving = true;
                    this.move(e);
                },
                getPosition:function(e){
                    var x = e.clientX - $('#canvas').offset().left;
                    var y = e.clientY - $('#canvas').offset().top + $('body').scrollTop();
                    return {x:x,y:y};
                },
                _findBox:function(coords){
                    var box = null;
                    this.boxes.forEach(function(b){
                        if(b.contains(coords.x,coords.y)){
                            box = b;
                        }
                    });
                    return box;
                },
                up:function(e){
                    if(this.card == null){return;}
                    var coords = this.getPosition(e);
                    var box = this._findBox(coords);
                    // Check if card is in a box
                    if(box!=null && box.card == null){
                        this.card.setDirectCoordinates(box.x+5,box.y+5);
                        box.card = this.card;
                    }else{
                        // Position the card at the original place
                        this.card.setDirectCoordinates(this.card.originPos.x,this.card.originPos.y);
                    }
                    this.moving = false;
                    this.card = null;
                }
            }
        },
        gameMode:{
            cards:[],
            enable:function(){
                console.log("enable");
                $('#canvas').bind('mousedown.play',function(e){Table.behaviours.gameMode._down(e);});
                // Not show call button if player first
                Actions.build('call');
            },
            disable:function(){
                console.log("disable");
                $('#canvas').unbind('mousedown.play');
                Actions.empty();
                this.cards = [];
            },
            _down:function(e){
                var coords = EventHelper.getPosition(e);
                var card = CardManager.findSelectCards(PlayerManager.getPlayerUser(),coords.x,coords.y);
                if(card == null){return;}
                this._checkCard(card);
            } ,
            _checkCard:function(card){
                card.checked = !card.checked;
                if(card.checked){
                    this.cards.push(card);
                }
                else{
                    var pos = this.cards.indexOf(card);
                    if(pos!=-1){
                        this.cards.splice(pos,1);
                    }
                }
                var _self = this;
                if(this.cards.length){
                    Actions.build('play','call');
                }else{
                    Actions.build('call');
                }
            },
            playBomb:function(bomb){
                var bombCombinaison = new Combinaison(bomb.type,bomb.high,bomb.nb);
                bomb.cards.forEach(function(c){
                    bombCombinaison.cards.push({value:c.value,color:c.color});
                });

                SenderManager.playBomb(bombCombinaison);
            },
            playFold:function(){
                var cards = this.cards.map(function(c){return c.card;});

                try{
                    var fold = CombinaisonsValidator.check(cards);
                    // Mahjong case
                    if(cards[0].value == 1){
                        MahjongPanel.open(function(value){
                            fold.mahjongValue = value;
                            SenderManager.sendCards(fold);
                        });
                    }else{
                        SenderManager.sendCards(fold);
                    }
                }catch(e){
                    alert("ERROR : " + e);
                }
            }
        }
    }
}

