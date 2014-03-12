/* Represent a player */

function Player(orientation,visible){
	this.orientation = orientation;
	this.cards = [];	// List of cards, sorted
	this.folds = [];	// List of cards wins
	this.visible = visible || false;
	this.name;
	
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
	
	this.setName = function(name){
		this.name = name;
	}
}

function Team(player1,player2){
	this.player1 = player1;
	this.player2 = player2;
	
	this.getScore = function(){
		return this.player1.countScore() + this.player2.countScore();
	}
}