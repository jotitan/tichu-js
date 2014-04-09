/* User action */

var Actions = {
	div:null,
	init:function(){
	    this.div = $('#actions');
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
			var button = $('<button>' + action.name + '</button>');
			button.bind('click',action.fct);
            this.div.append(button);
		},this);
	},
	empty:function(){
		this.div.empty()
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
                SenderMessage.callGame();
                Actions.empty();
            }
        },
        grandTichu:{
            name:"Grand Tichu",
            fct:function(){
                SenderManager.annonceGrandTichu();
            }
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
                Table.behaviours.changeMode.endChangeCards();
            }
        }
	}
}
