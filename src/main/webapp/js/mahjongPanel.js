/**
 * Show a panel to select mahjong value
 */

var MahjongPanel = {
    div:null,
    value:null,
    callback:null,
    nbCols:5,
    space:15,
    height:ComponentManager.variables.heightCard,
    width:ComponentManager.variables.widthCard,
    init:function(id){
        this.div = $('#' + id);
        this.div.dialog({
            autoOpen:false,
            title:'Mahjong choice',
            width:240,
            height:300,
            buttons:[
                {text:'No value',click:function(){MahjongPanel.define();}},
                {text:'Define',click:function(){MahjongPanel.define();}}
            ]
        });
        $('canvas',this.div).bind('mousedown.swap',function(e){MahjongPanel.getValue(e);});
        this.draw();
    },
    open:function(callback){
        this.callback = callback;
        this.div.dialog('open');
    },
    draw:function(selectedValue){
        var cardCanvas = $('canvas',this.div).get(0).getContext('2d');
        cardCanvas.clearRect(0,0,200,160);
        for(var i = 2 ; i <= 14 ; i++){
            var value = i < 11 ? i : i == 11 ? 'V' : i == 12 ? 'D' : i == 13 ? 'K':'A';
            var selected = selectedValue!=null && selectedValue == i ? 3:1;
            drawCard(cardCanvas,
                (i-2)%this.nbCols*(this.width+this.space),
                Math.floor((i-2)/this.nbCols)*(this.height+this.space),
                0,false,this.width,this.height,{val:value,color:"green"},
                null,"S",selected);
        }
    },
    getValue:function(e){
        var x = e.clientX - $('canvas',this.div).offset().left;
        var y = e.clientY - $('canvas',this.div).offset().top + $('body').scrollTop();

        var axeX = Math.floor(x/(this.width+this.space));
        var axeY = Math.floor(y/(this.height+this.space));

        if( x>=axeX*(this.width+this.space)
            && x<=axeX*(this.width+this.space)+this.width
            && y>=axeY*(this.height+this.space)
            && y<=axeY*(this.height+this.space) + this.height){
                this.value = 2 + this.nbCols * axeY + axeX;
                this.draw(this.value);
        }
    },
    define:function(){
        this.callback(this.value);
        this.div.dialog('close');
    }
}