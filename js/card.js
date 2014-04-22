
var STATUS_CARD = {
	NO_STATUS_CARD:0,
	DISTRIBUTED_CARD:1,
	CHANGED_CARD:2,
	TABLE_CARD:3,
	PLAYED_CARD:4
}

/* Represent the value of phoenix card */
function JokerCard(value){
	this.value = value;
	this.color = color;
}

function Card(value,color){
	this.value = value || 0;
	this.color = color || 'white';
	this.player = null;
	this.status = STATUS_CARD.NO_STATUS_CARD;
	this.id = color + value;
	this.checked = false;

	this.drawing = new DrawingCard(0,0,this);

	this.isPhoenix = function(){
		return false;
	}

    this.setChecked = function(checked){
        this.checked = checked;
        this.drawing.checked = checked;
    }

	this.getValue = function(){
		return value;
	}

	this.getScore = function(){
		switch(this.value){
			case 5:return 5;
			case 10:return 10;
			case 13:return 10;
		}
		return 0;
	}

	/* Position de la carte */
	this.setPlayer = function(player,pos){
		this.player = player;
		this.drawing.setOrientation(player.orientationTable,pos);
	}
	
	this.setStatus = function(status){
		this.status = status;
		this.drawing.status = status;
	}

	this.setRecto = function(recto){
	    this.drawing.recto = recto;
	}

	this.toString = function(){
		return value + " " + color;
	}
}

function MahjongCard(){
	Card.call(this,1);
	
	this.drawing = new ImgCard(0,0,"img/mahjong.png",this);	
}

function DogsCard(){
	Card.call(this,15);
	this.drawing = new ImgCard(0,0,"img/dogs.png",this);	
}

function PhoenixCard(){
	Card.call(this,16);
	this.drawing = new ImgCard(0,0,"img/phoenix.png",this);
	this.replaceValue == null;

	this.getValue = function(){
        return this.replaceValue || this.value;
	}
	this.isPhoenix = function(){
		return true;
	}
	
	this.getScore = function(){return -25;}
}

function DragonCard(){
	Card.call(this,17);
	this.drawing = new ImgCard(0,0,"img/dragon.png",this);
	
	this.getScore = function(){return 25;}
}

function ImgCard(x,y,img,card){
	DrawingCard.call(this,x,y,card);
	this.img = new Image();
	this.img.src = img;
	
	this._drawCard = function(canvas){
		drawCard(canvas,this.x,this.y,this.pos,this.checked,this.width,this.height,null,this.img,this.orientation);
	}
}

/* Represent the card of others players. No cheat cause the browser doesn't know the real value */
function EmptyCard(pos){
    Card.call(this,pos,'');
    this.drawing.recto = true;

    this.setRecto = function(recto){
        this.drawing.recto = true;
    }
}

function DrawingCard(x,y,card){
	Component.call(this);
	this.card = card;
	this.x = x;
	this.y = y;
	this.pos = 0;
	this.width = 25;
	this.height = 40;
	this.value = null;
	this.color = card.color;
	this.recto = false;
	this.imgRecto = new Image();
	this.imgRecto.src='img/recto.png';
	this.checked = false;
	this.deep = 0;
	
	switch(card.value){
		case 11 :this.value='V';break;
		case 12 :this.value='D';break;
		case 13 :this.value='K';break;
		case 14 :this.value='A';break;
		default : this.value = card.value;
	}

	this.setOrientation = function(orientation,pos,nb){		
		this.orientation = orientation;
		switch(orientation){
			case "C" : this.x = 150 + nb*5;this.y = 80 + nb*15;break;
			case "N" : this.x = ComponentManager.variables.width-140;this.y = this.height+10;break;
			case "S" : this.x = 140;this.y = ComponentManager.variables.height-this.height-10;break;
			case "O" : this.x = this.height+10;this.y = 140;break;
			case "E" : this.x = ComponentManager.variables.width-this.height-10;this.y = ComponentManager.variables.height-140;break;
		}
		this.setPosition(pos);
	}
	
	this.setPosition = function(pos){
		this.pos = pos*12;
	}

	this.setDirectCoordinates = function(x,y){
		this.x = x;
		this.y = y;
		this.pos = 0;
	}
	
	this.getCoords = function(){
		return {x:this.x+this.pos,y:this.y};
	}
	
	this.draw = function(canvas){
		/* Case when displayed */
		if(this.status !=STATUS_CARD.DISTRIBUTED_CARD && this.status != STATUS_CARD.TABLE_CARD){return;}
		
		if(this.recto){
			return this.drawRecto(canvas);
		}
		this._drawCard(canvas);
	}
	
	this._drawCard = function(canvas){
		drawCard(canvas,this.x,this.y,this.pos,this.checked,this.width,this.height,{val:this.value,color:this.color},null,this.orientation);		
	}
	
	this.drawRecto = function(canvas){
		drawCard(canvas,this.x,this.y,this.pos,this.checked,this.width,this.height,null,this.imgRecto,this.orientation);		
	}
	
	this.contains = function(x,y){
		return x>=this.x+this.pos && x <= this.x+this.pos+this.width && y>=this.y && y<=this.y + this.height;
	}
}

var COLORS = ["red","green","blue","black"];

var CardManager = {
	cards:[],
	emptyCards:[],
	cardsByValue:[],
	mahjongCard:null,
	init:function(){
		COLORS.forEach(function(color){
			for(var i = 2 ; i <= 14 ; i++){
				this.cards.push(new Card(i,color));
			}
		},this);
		this.mahjongCard = new MahjongCard();
		this.cards.push(this.mahjongCard);
		this.cards.push(new PhoenixCard());
		this.cards.push(new DragonCard());
		this.cards.push(new DogsCard());
		this.cards.forEach(function(c){
		    this.cardsByValue[c.value+c.color] = c;
		},this);

		this.sortCards();
	},
	get:function(value,color){
        return this.cardsByValue[value+color];
	},
	/* Used to determine which card is up */
	sortCards:function(){
		this.cards.sort(function(c1,c2){
			if(c1.drawing.deep!=c2.drawing.deep){
				return c1.drawing.deep - c2.drawing.deep;
			}
			if(c1.getValue() != c2.getValue()){
				return c1.getValue() - c2.getValue();
			}
			return c1.color > c2.color ? 1 : c1.color < c2.color ? -1 : 0;
		});
	},
	/* Reset all cards */
	newGame:function(){
		this.cards.forEach(function(c){c.player = null;c.drawing.recto=false;c.setStatus(STATUS_CARD.NO_STATUS_CARD);});
		PlayerManager.players.forEach(function(p){p.cards = [];});
	},
	findSelectCards:function(joueur,x,y){
		for(var i = joueur.cards.length-1 ; i >= 0; i--){
			if(joueur.cards[i].drawing.contains(x,y)){
				return joueur.cards[i].drawing;
			}
		}
		return null;
	}
}

/* Manage cards actually played */
var Plateau = {
	turn:0,		// Turn of game
	folds:[],	// List of folds on table, fold contains many cards
	playFold:function(cards,player){
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
			c.drawing.setOrientation("C",i,this.folds.length);
			c.drawing.deep = this.folds.length;
            if(player.equals(PlayerManager.getPlayerUser())){
			    player.playCard(c);
            }   else{
                player.removeEmptyCards(1);
            }
		},this);

		this.folds.push(cards);
		// Reorganize cards
		player.sortCards();
		CardManager.sortCards();
	},
    resetTurn:function(){
        this.folds.forEach(function(fold){
            fold.forEach(function(card){
                card.setStatus(STATUS_CARD.PLAYED_CARD);
            });
        });
        this.folds = [];
    },
	/* Check that combinaison is correct */
	checkCombinaison:function(cards){
		
	}
}

function Bomb(cards,type){
    this.cards = cards;
    this.type = type;
    this.high = cards[0].value;
    this.nb = cards.length;
}


var Sorter = {
    cards:function(cards){
        cards.sort(function(c1,c2){
            if(c1.getValue() != c2.getValue()){
                return c1.getValue() - c2.getValue();
            }
            return c1.color > c2.color ? 1 : c1.color < c2.color ? -1 : 0;
        });
    }
}