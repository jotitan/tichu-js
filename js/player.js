/* Represent a player */

function Player(orientation,name,visible){
	this.orientation = orientation;
	this.cards = [];	// List of cards, sorted
	this.folds = [];	// List of cards wins
	this.visible = visible || false;
	this.name;

    this.drawing = new TitleBox(name,orientation);
    ComponentManager.add(this.drawing);

    this.setName = function(name){
        this.name = name;
        this.drawing.setName(name);
    }

	/* Sort by number and color */
	this.sortCards = function(){
		this.cards.sort(function(c1,c2){
			if(c1.value != c2.value){
				return c1.value - c2.value;
			}
			return c1.color > c2.color ? 1 : c1.color < c2.color ? -1 : 0;
		});
		this.cards.forEach(function(c,i){
			c.drawing.setPosition(i);
		});
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
			c.drawing.recto = true;
		});
	}
	
	this.showVerso = function(){
		this.cards.forEach(function(c){
			c.drawing.recto = false;
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
    team1:null,
    team2:null,
    joueur:null,
    init:function(){
        this.players.push(new Player("N","Joueur 1"));
        this.players.push(new Player("E","Joueur 2"));
        this.players.push(new Player("S","Joueur 3",true));
        this.players.push(new Player("O","Joueur 4"));
        this.players.forEach(function(p){
            this.playersByOrientation[p.orientation] = p;
        },this);

        this.team1 = new Team(this.players[0],this.players[2]);
        this.team2 = new Team(this.players[1],this.players[3]);
        this.joueur = this.players[1];
    },
    getByOrientation:function(orientation){
        return this.playersByOrientation[orientation];
    },
    getPlayers:function(){
        return this.players;
    },
    firstPlayer:function(){
        return CardManager.mahjongCard.player;
    }
}