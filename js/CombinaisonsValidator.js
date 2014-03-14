/* Manage and test cards during a turn */


/* @param : type of combinaison (CombinaisonType) */
/* @param hight : higher card of the combinaison (use the first of the series) */
/* @param nb : number of card (use in straight, pair straight, straight bomb */
function Combinaison(type,high,nb){
	this.type = type;
	this.high = high;
	this.nb = nb;
}

var CombinaisonType = {
	SINGLE:'Single',
	PAIR:'Pair',
	STRAIGHTPAIR:'Straight of Pair',
	BRELAN:'Brelan',
	FULLHOUSE:'Full',
	STRAIGHT:'Straight',
	STRAIGHTBOMB:'Straight Bomb of color',
	SQUAREBOMB:'Square Bomb'	
}

var CombinaisonsValidator = {
	previous:null,
	resetTurn:function(){
		this.previous = null;
	},
	check:function(cards){
		var combinaison = this.detect(cards);
		if(combinaison == null){
			throw "Unknown combinaison";
		}
		if(this.previous != null){
			if(this.previous.type != combinaison.type){
				throw "Combinaison with different type, " + this.previous.type + " asked";
			}
			
		}		
		this.previous = combinaison;
	},
	detect:function(cards){
		if(cards.length == 0){
			throw "Empty Hand";
		}
		
		cards.sort(function(c1,c2){
			if(c1.value == c2.value){
				return c1.color > c2.color ? 1 : -1;
			}
			return c1.value - c2.value;
		});
	
		if(cards.length == 1){
			return new Combinaison(CombinaisonType.SINGLE,cards[0].value);
		}
		if(cards.length == 2){
			return this._checkSame(cards,CombinaisonType.PAIR);
		}
		if(cards.length == 3){
			return this._checkSame(cards,CombinaisonType.BRELAN);
		}
		if(cards.length % 2 == 0){
			return this._checkStraightPair(cards);
		}
		if(cards.length == 5){
			var combinaison = this._checkFullHouse(cards);
			if(combinaison!=null){
				return combinaison;
			}
		}
		
	},
	_checkSame:function(cards,type){
		var value = cards[0].value;
		return cards.every(function(c){return c.value == value;})
			? new Combinaison(type,value) : null;		
	},
	_checkBombs:function(cards){
		if(cards.length == 4){
			var value = cards[0].value;
			if(cards.every(function(c){return value == c.value;})){
				return new Combinaison(CombinaisonType.SQUAREBOMB,cards[0]);
			}else{
				return null;
			}
		}
		var color = cards[0].color;
		var value = cards[0].value;
		var check = cards.every(function(c,i){
			return  i == 0 || (c.color == color && c.value == ++value);
		});
		return check ? new Combinaison(CombinaisonType.STRAIGHTBOMB,value,cards.length):null;	
	},
	_checkStraightPair:function(cards){
		var value = null;
		var check = cards.every(function(c,i){
			if(i%2 == 0){
				value = c.value;
				return true;
			}else{
				return value == c.value;
			}			
		});
		
		return check ? new Combinaison(CombinaisonType.STRAIGHTPAIR,cards[0].value,cards.length/2) : null;
	},
	_checkFullHouse:function(cards){
		var value = cards[0].value;
		var change = 0;
		var pos = 0;
		cards.forEach(function(c,i){
			if(c.value!=value){
				change++;
				value = c.value;
				pos = i;
			}
		});
		if(change == 1){
			var high = pos == 3 ? cards[0].value : cards[4].value;
			return new Combinaison(CombinaisonType.FULLHOUSE,high) 
		}
		return null;
	}

}