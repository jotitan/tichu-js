/* User action */

var Actions = {
	div:$('#actions'),
	build:function(){
	  var actions = arguments.map(function(name){
	    return Actions.actions[name];
	  });
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
        grand-tichu:{
            name:"Grand Tichu",
            fct:function(){
                SenderManager.annonceGrandTichu();
            }
        },
        last-cards:{
            name:"Last cards",
            fct:function(){
                SenderManager.showLastCards();
            }
        },
        swap-cards:{
            name:"Change",
            fct:function(){
                Table.behaviours.changeMode.validate();
            }
        },
        accept-cards:{
            name:"Ok",
            fct:function(){
                Table.behaviours.changeMode.endChangeCards();
            }
        }
	}
}
