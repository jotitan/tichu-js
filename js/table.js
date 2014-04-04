/* Manage the table of game */
/* Behave : build table, show new player (name, connected or not) */


var Table = {
    display:function(data){
        data.players.forEach(function(p){
            var pl = PlayerManager.getByOrientation(p.orientation);
            pl.setName(p.name);
        });
    },
    connectPlayer:function(orientation,currentUser){
        PlayerManager.getByOrientation(orientation).connect(currentUser);
    },
    disconnectPlayer:function(orientation,currentUser){
        PlayerManager.getByOrientation(orientation).drawing.setOffline();
    },
    receiveCards:function(cards){
        var cl = CardManager.get(cards.toLeft.value,cards.toLeft.color);
        var cp = CardManager.get(cards.toPartner.value,cards.toPartner.color);
        var cr = CardManager.get(cards.toRight.value,cards.toRight.color);

        PlayerManager.getPlayerUser().giveCard(cl);
        PlayerManager.getPlayerUser().giveCard(cp);
        PlayerManager.getPlayerUser().giveCard(cr);
        this.behaviours.changeMode.showChangedCards(cl,cp,cr);
    },
    distribute:function(cards){
        PlayerManager.players.forEach(function(p){
           if(p.name == PlayerManager.getPlayerUser().name){
            var user = PlayerManager.getPlayerUser();
            cards.forEach(function(card){
                user.giveCard(CardManager.get(card.value,card.color));
            });
           }else{
            p.createEmptyCards(14);
           }
           p.sortCards();
        });
    },
    behaviours:{
        changeMode:{
        boxes:[],
            enable:function(){
                /* Add box to drop the card */
                this._buildBoxes();
                this.mouseController.enable(this.boxes);
                Actions.create([{name:"Change",fct:function(){Table.behaviours.changeMode.validate();}}]);
            },
            _buildBoxes:function(){
                var width = ComponentManager.variables.width/2;
                var height = ComponentManager.variables.height-200;

                this.boxes.push(new BoxCard(width-75,height,35,50));
                this.boxes.push(new BoxCard(width-25,height-20,35,50));
                this.boxes.push(new BoxCard(width+25,height,35,50));
                this.boxes.forEach(function(b){ComponentManager.add(b);});
            },
            validate:function(){
                var check = this.boxes.every(function(b){return b.card!=null;});
                if(!check){
                    alert("impossible, choose one card for each");
                    return;
                }
                this.boxes.forEach(function(b){
                    PlayerManager.getPlayerUser().playCard(b.card.card);
                    b.card.card.setStatus(STATUS_CARD.CHANGED_CARD);
                });
                SenderManager.changeCards(this.boxes[0].card.card,this.boxes[1].card.card,this.boxes[2].card.card);
            },
            disable:function(){
                this.boxes.forEach(function(b){
                    b.card.card
                    ComponentManager.remove(b);
                });
                PlayerManager.getPlayerUser().sortCards();
                this.mouseController.disable();
            },
            /* Display received cards */
            showChangedCards:function(fromLeft,fromPartner,fromRight){
                fromLeft.card.setDirectCoordinates(this.boxes[0].x+5,this.boxes[0].y+5);
                fromPartner.card.setDirectCoordinates(this.boxes[1].x+5,this.boxes[1].y+5);
                fromRight.card.setDirectCoordinates(this.boxes[2].x+5,this.boxes[2].y+5);

                Actions.create([{name:"Ok",fct:function(){Table.behaviours.changeMode.endChangeCards();}}]);
            },
            endChangeCards:function(){
                this.boxes = [];
                PlayerManager.getPlayerUser().sortCards();

            },
            mouseController:{
                moving:false,
                card:null,
                boxes:null,
                disable:function(){
                    this.boxes = null;
                    $('#canvas').unbind('mousemove.swap');
                    $('#canvas').unbind('mouseup.swap');
                    $('#canvas').unbind('mousedown.swap');
                },
                enable:function(boxes){
                    this.boxes = boxes;
                    var _self = this;
                    $('#canvas').bind('mousemove.swap',function(e){_self.move(e);});
                    $('#canvas').bind('mouseup.swap',function(e){_self.up(e);});
                    $('#canvas').bind('mousedown.swap',function(e){_self.down(e);});
                },
                move:function(e){
                    if(!this.moving || !this.card){return;}
                    var coords = this.getPosition(e);
                    this.card.setDirectCoordinates(coords.x-10,coords.y-10);
                },
                down:function(e){
                    var coords = this.getPosition(e);
                    var box = this._findBox(coords);
                    if(box !=null){
                        box.card = null;
                    }
                    var card = CardManager.findSelectCards(PlayerManager.getPlayerUser(),coords.x,coords.y);
                    if(card == null){return;}
                    this.card = card;
                    this.card.originPos = card.originPos || card.getCoords();
                    this.moving = true;
                    this.move(e);
                },
                getPosition:function(e){
                    var x = e.clientX - $('#canvas').offset().left;
                    var y = e.clientY - $('#canvas').offset().top + $('body').scrollTop();
                    return {x:x,y:y};
                },
                _findBox:function(coords){
                    var box = null;
                    this.boxes.forEach(function(b){
                        if(b.contains(coords.x,coords.y)){
                            box = b;
                        }
                    });
                    return box;
                },
                up:function(e){
                    if(this.card == null){return;}
                    var coords = this.getPosition(e);
                    var box = this._findBox(coords);
                    // Check if card is in a box
                    if(box!=null){
                        this.card.setDirectCoordinates(box.x+5,box.y+5);
                        box.card = this.card;
                    }else{
                        // Position the card at the original place
                        this.card.setDirectCoordinates(this.card.originPos.x,this.card.originPos.y);
                    }
                    this.moving = false;
                    this.card = null;
                }
        },
            /* Can select the card */
            enablePlayMode:function(){
                this.playMouseController.enable();
            },
            playMouseController:{
                cards:[],
                disable:function(){
                    $('#canvas').unbind('mousedown.play');
                },
                enable:function(){
                    var _self = this;
                    $('#canvas').bind('mousedown.play',function(e){_self._down(e);});
                },
                _down:function(e){
                    var coords = EventHelper.getPosition(e);
                    var card = CardManager.findSelectCards(PlayerManager.getPlayerUser(),coords.x,coords.y);
                    if(card == null){return;}
                    this._checkCard(card);
                }
                ,_checkCard:function(card){
                    card.checked = !card.checked;
                    if(card.checked){
                        this.cards.push(card);
                    }
                    else{
                        var pos = this.cards.indexOf(card);
                        if(pos!=-1){
                            this.cards.splice(pos,1);
                        }
                    }
                    var _self = this;
                    if(this.cards.length){
                        Actions.create([{
                            name:"Jouer",
                            fct:function(){
                                var cards = _self.cards.map(function(c){return c.card;});
                                console.log(CombinaisonsValidator.detect(cards));
                            }
                        }]);
                    }else{
                        Actions.empty();
                    }
                }
            }
        }
    }
}

