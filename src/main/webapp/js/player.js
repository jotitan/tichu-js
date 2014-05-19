/* Represent a player */

function Player(orientation,name,visible){
	this.orientation = orientation;
	this.orientationTable = orientation;    // Orientation on the table, user always on the bottom
	this.cards = [];	// List of cards, sorted
	this.folds = [];	// List of cards wins
	this.playFoldOnTurn = 0; // Number of fold played on this turn
	this.visible = visible || false;
	this.name = name;
    this.connected = false;   // Player connected
    this.select = false;    // Current player
    this.served = false;    // Player has all cards (14)
    this.bombs = [];    // List of available bomb
    this.annonce = null;

    this.equals = function(player){
        if(player == null){
            return false;
        }
        return this.orientation == player.orientation;
    }

    this.drawing = new TitleBox(name,orientation,this);
    ComponentManager.add(this.drawing);

    this.setAnnonce = function(annonce){
        this.annonce = annonce;
        if(annonce == null){
            this.drawing.annonce = null;
        }else{
            switch(annonce){
                case "GRAND_TICHU":this.drawing.annonce = "GT";break
                case "TICHU":this.drawing.annonce = "T";break
            }
        }
    }

    this.isAnnonce = function(){
        return this.annonce!= null;
    }

    this.setSelected = function(select){
        this.select = select;
        this.drawing.select = select;
    }

    this.setName = function(name){
        this.name = name;
        this.drawing.setName(name);
    }

    /* @param user : if true, the player go the south */
    this.connect = function(user){
        this.connected = true;
        if(user){
            PlayerManager.updateOrientation(this);
        }
        this.drawing.setOnline();
    }

    this.setOrientationTable = function(orientationTable){
        this.orientationTable = orientationTable;
        this.drawing.changeOrientation(orientationTable);
    }

    this.detectBombs = function(){
        this.bombs = BombDetector.detect(this.cards);
    }

    this.displayBombs = function(){
        this.bombs.forEach(function(bomb){
           Actions.addBombAction(bomb);
        });
    }
	/* Sort by number and color */
    /* @param fullSort : init also the orientation */
	this.sortCards = function(fullSort){
		Sorter.cards(this.cards);
		this.cards.forEach(function(c,i){
			if(fullSort){
                c.drawing.setOrientation(this.orientationTable,i);
            }
            else{
                c.drawing.setPosition(i);
            }
		},this);
		if(!this.visible){
			this.showRecto();
		}
	}
	
	this.countScore = function(){
		return this.folds.reduce(function(score,card){return score+card.getScore();},0);		
	}
	
	this.playFold = function(fold){
        this.drawing.playCall = false;
		try{
		    if(fold.mahjongValue!=null){
                Chat.info("A " + fold.mahjongValue + " is needed !")
		    }
            var cards = fold.cards.map(function(c){
                var card = CardManager.get(c.value, c.color);
                if(card.isPhoenix){
                    card.replaceValue = fold.jokerValue;
                }
                card.bombs.forEach(function(bomb){
                    this.removeBomb(bomb);
                },this);
                return card;
            },this);
            Table.doPlayFold(cards,this);

            this.playFoldOnTurn++;
		}catch(impossible){
			alert("Impossible combinaison " + impossible);
		}
	}

	/* When user play a call */
	this.playCall = function(){
        this.drawing.playCall = true;
	}

    this.removeBomb = function(bomb){
        var idx = this.bombs.indexOf(bomb);
        if(idx > 0){
            this.bombs.splice(idx,1);
        }
        if(bomb.remove!=null){
            bomb.remove();
        }
    }

    this.playBomb = function(fold){
        this.playFold(fold);

    }
	
	this.showRecto = function(){
		this.cards.forEach(function(c){
			c.setRecto(false);
		});
	}
	
	this.showVerso = function(){
		this.cards.forEach(function(c){
			c.setRecto(true);
		});
	}
	
	/* Delete the card from the hand of the player */
	this.playCard = function(card){
		var pos = this.cards.indexOf(card);
		if(pos!=-1){
			this.cards.splice(pos,1);
		}
	}
	
	this.winFolds = function(folds){
		folds.forEach(function(fold){
			fold.forEach(function(c){
				this.folds.push(c);
			},this);
		},this);
	}

	this.giveCard = function(card){
	    this.cards.push(card);
		card.setPlayer(this,this.cards.length);
		card.setStatus(STATUS_CARD.DISTRIBUTED_CARD);
	}

    this.haveCard = function(card){
        return this.cards.indexOf(card) != -1;
    }

    this.removeCards = function(){
        this.cards = [];
    }

    /* Add empty card until player has nb cards */
    this.setNecessaryEmptyCards = function(nb){
        if(nb == this.cards.length){return;}
        if(nb < this.cards.length){
            this.removeEmptyCards(this.cards.length - nb);
        }else{
            this.createEmptyCards(nb - this.cards.length);
        }
    }

	this.createEmptyCards = function(nb){
	    for(var i = 0 ; i < nb ; i++){
	        var c = new EmptyCard(this.cards.length);
	        this.giveCard(c);
	        CardManager.emptyCards.push(c);
	    }
	    this.sortCards();
	}

	this.removeEmptyCards = function(nb){
	    for(var i = 0 ; i < nb ; i++){
	        var c = this.cards[this.cards.length -1];
	        this.cards.splice(this.cards.length-1,1);
	        var pos = CardManager.emptyCards.indexOf(c);
	        if(pos!=-1){
	            CardManager.emptyCards.splice(pos,1);
	        }
	    }
	}

	this.getNbCards = function(){
		return this.cards.length;
	}

	this.initRound = function(){
	    this.setAnnonce(null);
        this.served = false;
	}
}

function Team(player1,player2){
	this.player1 = player1;
	this.player2 = player2;
	
	this.getScore = function(){
		return this.player1.countScore() + this.player2.countScore();
	}
}


var PlayerManager = {
    players:[],
    playersByOrientation:[],
    playerUser:null,    // Player of the browser
    team1:null,
    team2:null,
    orientations : ["N","E","S","O"],
    currentPlayer:null,
    init:function(){
        this.orientations.forEach(function(o,i){
            this.players.push(new Player(o,"Joueur " + i,o == "S"));
        },this);
        this.players.forEach(function(p){
            this.playersByOrientation[p.orientation] = p;
        },this);

        this.team1 = new Team(this.players[0],this.players[2]);
        this.team2 = new Team(this.players[1],this.players[3]);
        this.playerUser = this.players[2];
    },
    nextPlayer:function(player){
        if(this.currentPlayer!=null){
            this.currentPlayer.setSelected(false);
        }
        this.currentPlayer = this.getByOrientation(player.orientation);
        this.currentPlayer.setSelected(true);
        if(!this.playerUser.served){
            return;
        }
        if(this.currentPlayer.equals(this.playerUser)){
            // Show option only if all cards are received
            Table.behaviours.gameMode.enable();
        }
        else{
            Table.behaviours.gameMode.disable();
        }
    },
    getByOrientation:function(orientation){
        return this.playersByOrientation[orientation];
    },
    getPlayerUser :function(){
        return this.playerUser;
    },
    /* Place the player at the good place and shift the others */
    updateOrientation:function(southPlayer){
        this.playerUser = southPlayer;
        southPlayer.setOrientationTable("S");
        var oldPos = this.orientations.indexOf(southPlayer.orientation);
        var newPos = this.orientations.indexOf("S");
        var shift = newPos -oldPos+4;
        this.players.forEach(function(p){
        if(p.orientation != southPlayer.orientation){
            var newOrientation = this.orientations[(this.orientations.indexOf(p.orientation) + shift)%4];
            p.setOrientationTable(newOrientation);
        }
       },this);
    },
    getPlayers:function(){
        return this.players;
    },
    firstPlayer:function(){
        return CardManager.mahjongCard.player;
    },
    winTurn:function(player){
        if(player){
            Chat.info("Player " + player.name + " win the turn");
        }
        this.resetTurn();
    },
    resetTurn:function(){
        Table.resetTurn();
        CombinaisonsValidator.resetTurn();
        this.players.forEach(function(p){
            p.playFoldOnTurn = 0;
        });
    },
    playFold:function(fold){
        var player = this.getByOrientation(fold.player);
        player.playFold(fold);
        CombinaisonsValidator.addFold(fold);
    },
    playBomb:function(bomb){
        var player = this.getByOrientation(bomb.player);
        player.playBomb(bomb);
        CombinaisonsValidator.addFold(bomb);
    },
    call:function(player){
        var player = this.getByOrientation(player.orientation);
        player.playCall();
        Chat.info(player.name + " CALL");
    }
}