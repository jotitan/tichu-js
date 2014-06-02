/* Manage and test cards during a turn */


/* Represent a fold */
/* @param : type of combinaison (CombinaisonType) */
/* @param hight : higher card of the combinaison (use the first of the series) */
/* @param nb : number of card (use in straight, pair straight, straight bomb */
function Combinaison(type,high,nb,jokerValue){
	this.type = type;
	this.high = high;
	this.nb = nb;
	if(nb == null){
	    throw "NB can't be null";
	}
	this.jokerValue = jokerValue;
	this.mahjongValue = null;
	this.cards=[];

    this.length = function(){
        return this.cards.length;
    }
}

var CombinaisonType = {
	SINGLE:'SINGLE',
	PAIR:'PAIR',
	STRAIGHTPAIR:'STRAIGHTPAIR',
	BRELAN:'BRELAN',
	FULLHOUSE:'FULLHOUSE',
	STRAIGHT:'STRAIGHT',
	STRAIGHTBOMB:'STRAIGHTBOMB',
	SQUAREBOMB:'SQUAREBOMB'
}

var CombinaisonsValidator = {
	previous:null,
    combinaisons:[],
	resetTurn:function(){
		this.previous = null;
        this.combinaisons = [];
	},
	addFold:function(fold){
        this.combinaisons.push(fold);
	    this.previous = fold;
	},
	removeLast:function(){
        if(this.combinaisons.length == 0){return;}
        this.combinaisons.splice(this.combinaisons.length-1,1);
        if(this.combinaisons.length>0){
            this.previous = this.combinaisons[this.combinaisons.length-1];
        }
        else{
            this.previous = null;
        }
	},
	check:function(cards){
		var combinaison = this.detect(cards);
        console.log(combinaison)
		if(combinaison == null){
			throw "Unknown combinaison";
		}
		if(this.previous != null){
			if(this.previous.type != combinaison.type){
				throw "Combinaison with different type, " + this.previous.type + " asked";
			}
            if(this.previous.high >= combinaison.high){
                throw "Combinaison is too low";
            }
            if(this.previous.nb != combinaison.nb){
                throw "Different number of card"
            }
			
		}
		cards.forEach(function(c){
		   combinaison.cards.push({value:c.value,color:c.color});
		});
		//this.previous = combinaison;
        //this.combinaisons.push(combinaison);
		return combinaison;
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

		// Find joker
		var ctx = this._createContext(cards);
		if(cards.length == 1){
			var value = cards[0].getValue();
			if(cards[0].isPhoenix()){
				value = this.previous != null && this.previous.high!=17 ? this.previous.high + 0.5 : 1.5;
			}
			return new Combinaison(CombinaisonType.SINGLE,value,1,value);
		}
		if(cards.length == 2){
			return this._checkSame(cards,CombinaisonType.PAIR,this._createContext(cards));
		}
		if(cards.length == 3){
			return this._checkSame(cards,CombinaisonType.BRELAN,this._createContext(cards));
		}
		if(cards.length % 2 == 0){
			var combinaison = this._checkStraightPair(cards,this._createContext(cards));
			if(combinaison!=null){
				return combinaison;
			}
		}
		if(cards.length == 5){
			var combinaison = this._checkFullHouse(cards,this._createContext(cards));
			if(combinaison!=null){
				return combinaison;
			}			
		}
		if(cards.length >=5){
			var combinaison = this._checkStraight(cards,this._createContext(cards));
			if(combinaison!=null){
				return combinaison;
			}			
		}
		return null;
		
	},
	_createContext:function(cards){
		var jokerCard = null;
		var joker = cards.some(function(c){
			if(c.isPhoenix()){
				jokerCard = c;
				c.replaceValue = null;
			}
			return c.isPhoenix()
		})
		return {joker:joker,card:jokerCard};
	},
	_checkSame:function(cards,type,ctx){
		var value = cards[0].value;
		return cards.every(function(c){
			if(c.value == value){return true;}
			if(ctx.joker){ctx.joker = false;ctx.card.replaceValue=value;return true;}
		}) ? new Combinaison(type,value,cards.length,ctx.card!=null?ctx.card.replaceValue:null) : null;
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
	_checkStraightPair:function(cards,ctx){
		var value = null;
		var nb = 0;
		var check = cards.every(function(c,i){
			if(i == 0 || (nb == 0 && c.value == value+1)){
			    value = c.value;
			    nb = 1;
			    return true;
			}
			if(nb == 1 && c.value == value){
			    nb = 0;
			    return true;
			}
			if(nb == 1 && ctx.joker && c.value == value+1){
			    ctx.joker = false;
			    ctx.card.replaceValue = value;
			    value = c.value;
			    nb = 1;
			    return true;
			}
			/* Last card phoenix. If replace last value or ever used */
			if(c.isPhoenix() && ((nb == 1 && ctx.joker) || !ctx.joker)){
			    if(ctx.joker){
			        ctx.card.replaceValue = value;
			    }
			    return true;
			}
			return false;
		});
		
		return check ? new Combinaison(CombinaisonType.STRAIGHTPAIR,cards[0].value,cards.length/2,ctx.card!=null ? ctx.card.replaceValue:null) : null;
	},
	_checkFullHouse:function(cards,ctx){
		var value = cards[0].value;
		var change = 0;
		var values = [];
		var pos=0;
		cards.forEach(function(c,i){
			if(c.value!=value){
				change++;
				value = c.value;
				pos = i;
			}
			if(!c.isPhoenix()){
				if(values[0] == null || values[0].value == value){
					if(values[0] == null){values[0] = {value:value,nb:0}}
					values[0].nb++;
				}
				else{
					if(values[1] == null){values[1] = {value:value,nb:0}}
					values[1].nb++;
				}
			}		
		});
		if(change == 1 || (change == 2 && ctx.card!=null)) {
			var high = pos == 3 ? cards[0].value : cards[3].value;
			if(change == 2){
				switch(values[0].nb){
					case 1 : ctx.card.replaceValue = values[0].value;values[1].value;break;
					case 2 : ctx.card.replaceValue = Math.max(values[0].value,values[1].value);high=ctx.card.replaceValue;break;
					case 3 : ctx.card.replaceValue = values[1].value;high = values[0].value;break;
				}
			}
			return new Combinaison(CombinaisonType.FULLHOUSE,high,5,ctx.card!=null?ctx.card.replaceValue:null) ;
		}
		return null;
	},
	_checkStraight:function(cards,ctx){
	    var value = cards[0].value;
		var check = cards.every(function(c,i){
			if(i==0 || c.value == ++value){
				return true;
			}			
			if(ctx.joker && c.value == value+1 && !c.isPhoenix()){						
				ctx.card.replaceValue = value++;
				ctx.joker = false;
				return true;
			}
			// Case joker, last card			
			if(c.isPhoenix()){
				if(ctx.joker){
					ctx.card.replaceValue = value == 15 ? cards[0].value-1 : value;
					ctx.joker = false;
				}
				return true;
			}
			return false
		});		
		value = Math.min(cards[0].value,ctx.card!=null?ctx.card.replaceValue:cards[0].value);
		if(check){
			return new Combinaison(CombinaisonType.STRAIGHT,value,cards.length,ctx.card!=null?ctx.card.replaceValue:null) ;
		}
		return null;
	}

}

var BombDetector = {
    /** @return : list of bomb */
    detect:function(cards){
        var copyCards = cards.map(function(c){return c;});

        copyCards.sort(function(a,b){
            if(a.color == b.color){
                return a.value - b.value;
            }
            return a.color > b.color;
        });

        var bombs = this._detectStraight(copyCards);
        this._detectSquares(copyCards).forEach(function(bomb){
           bombs.push(bomb);
        });
        return bombs;
    },
    _detectStraight:function(cards){
        var bombs = [];
        var color = cards[0].color;
        var value = cards[0].value;
        var tempCards = [];
        var nb = 1;
        for(var i = 1 ; i < cards.length ; i++){
            if(color!=cards[i].color || value+1 != cards[i].value){
                if(nb>=5){  // Got a bomb
                    bombs.push(new Bomb(tempCards,CombinaisonType.STRAIGHTBOMB));
                }
                nb = 0;
                tempCards = [];
            }
            color = cards[i].color;
            value = cards[i].value;
            tempCards.push(cards[i]);
            nb++;
        }
        if(nb>=5){  // Got a bomb
            bombs.push(new Bomb(tempCards,CombinaisonType.STRAIGHTBOMB));
        }
        return bombs;
    },
    _detectSquares:function(cards){
        var bombs = [];
        var tempCards = [];
        var previous = null;
        var nb = 0;
        cards.forEach(function(card){
            if(previous == null || card.value == previous){

            }else{
                nb = 0;
                tempCards = [];
            }
            nb++;
            tempCards.push(card);
            previous = card.value;
            if(nb == 4){
                bombs.push(new Bomb(tempCards,CombinaisonType.SQUAREBOMB));
            }
        });
        return bombs;
    }
}