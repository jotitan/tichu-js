/* User action displayed in buttons */

var Actions = {
	div:null,
	init:function(){
	    this.div = $('#actions');
	},
	addBombAction:function(bomb){
	    var title = "Bomb " + bomb.type.substr(0,2);
        if(bomb.type == CombinaisonType.SQUAREBOMB){
            title+= " " + bomb.high;
        }else{
            title+= " " + bomb.cards[0].value + " - " + bomb.cards[bomb.cards.length -1].value + " " + bomb.cards[0].color;
        }
        var id = title.replace(/ /g,'');
        var btn = {name:title,fct:function(){
            Table.behaviours.gameMode.playBomb(bomb);
        },class:"bomb",id:id};
        bomb.remove = function(){
            Actions.deleteById(id);
        };
        this._addButton(btn);
	},
	build:function(){
	  var actions = [];
        for(var i in arguments){
            actions.push(Actions.actions[arguments[i]]);
        }
	  this.create(actions);
	},
	create:function(actions){
		this.empty();
		actions.forEach(function(action){
            this._addButton(action);
		},this);
	},
    _addButton:function(action){
        var button = $('<button>' + action.name + '</button>');
        if(action.class){
            button.attr('class',action.class);
        }
        if(action.id){
            button.attr('id',action.id);
        }
        button.bind('click',action.fct);
        this.div.append(button);
    },
	empty:function(deleteAll){
		if(deleteAll){
            this.div.empty();
        }
        else{
            this.div.find('button:not([class])').remove();
        }
	},
    deleteByClass:function(cssClass){
        this.div.find('button[class="' + cssClass + '"]').remove();
    },
    deleteById:function(id){
        this.div.find('#' + id).remove();
    },
	actions:{
	    play:{
	        name:"Play",
            fct:function(){
                Table.behaviours.gameMode.playFold();
            }
	    },
        call:{
            name:"Call",
            fct:function(){
                SenderManager.callGame();
                Actions.empty();
            }
        },
        grandTichu:{
            name:"Grand Tichu",
            fct:function(){
                SenderManager.annonceGrandTichu();
            }
        },
        tichu:{
            name:"Tichu",
            fct:function(){
                SenderManager.annonceTichu();
                Actions.deleteByClass('tichu');
            } ,
            class:"tichu"
        },
        lastCards:{
            name:"Last cards",
            fct:function(){
                SenderManager.showLastCards();
            }
        },
        swapCards:{
            name:"Change",
            fct:function(){
                Table.behaviours.changeMode.validate();
            }
        },
        acceptCards:{
            name:"Ok",
            fct:function(){
                PlayerManager.getPlayerUser().served = true;    // get all cards
                Table.behaviours.changeMode.endChangeCards();
            }
        }
	}
}
