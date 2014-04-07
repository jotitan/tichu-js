/* Represent a player */

function Player(orientation,name,visible){
	this.orientation = orientation;
	this.orientationTable = orientation;    // Orientation on the table, user always on the bottom
	this.cards = [];	// List of cards, sorted
	this.folds = [];	// List of cards wins
	this.visible = visible || false;
	this.name = name;
    this.connect = false;
    this.select = false;

    this.drawing = new TitleBox(name,orientation);
    ComponentManager.add(this.drawing);

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
        this.connect = true;
        if(user){
            PlayerManager.updateOrientation(this);
        }
        this.drawing.setOnline();
    }

    this.setOrientationTable = function(orientationTable){
        this.orientationTable = orientationTable;
        this.drawing.changeOrientation(orientationTable);
    }

	/* Sort by number and color */
    /* @param fullSort : init also the orientation */
	this.sortCards = function(fullSort){
		this.cards.sort(function(c1,c2){
			if(c1.value != c2.value){
				return c1.value - c2.value;
			}
			return c1.color > c2.color ? 1 : c1.color < c2.color ? -1 : 0;
		});
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
	
	this.playFold = function(cards){
		try{
			Plateau.playFold(cards);
		}catch(impossible){
			alert("Impossible combinaison");
		}
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

	this.createEmptyCards = function(nb){
	    for(var i = 0 ; i < nb ; i++){
	        var c = new EmptyCard(i);
	        this.giveCard(c);
	        CardManager.emptyCards.push(c);
	    }
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
    }
}