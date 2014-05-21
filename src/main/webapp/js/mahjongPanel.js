/**
 * Created by Titan on 21/05/14.
 */

var MahjongPanel = {
    div:null,
    value:null,
    init:function(id){
        this.div = $('#' + id);
        this.div.dialog({
            autoOpen:false,
            title:'Mahjong choice',
            width:280,
            height:300,
            buttons:[
                {text:'No value',click:function(){}},
                {text:'Define',click:function(){defineMahjong();}}
            ]
        });
        $('canvas',this.div).bind('mousedown.swap',function(e){MahjongPanel.getValue(e);});
        this.draw();
    },
    open:function(callback){
        this.div.panel('open');
    },
    draw:function(selectedValue){
        var cardCanvas = $('canvas',this.div).get(0).getContext('2d');
        cardCanvas.clearRect(0,0,250,200);
        for(var i = 2 ; i <= 14 ; i++){
            var value = i < 11 ? i : i == 11 ? 'V' : i == 12 ? 'D' : i == 13 ? 'K':'A';
            var selected = selectedValue!=null && selectedValue == i ? 3:1;
            drawCard(cardCanvas,(i-2)%5*50,Math.floor((i-2)/5)*55,0,false,25,40,{val:value,color:"green"},null,"S",selected);
        }
    },
    getValue:function(e){
        var x = e.clientX - $('canvas',this.div).offset().left;
        var y = e.clientY - $('canvas',this.div).offset().top + $('body').scrollTop();

        var axeX = Math.floor(x/50);
        var axeY = Math.floor(y/55);

        if(x>=axeX*50 && x<=axeX*50+25 && y>=axeY*55 && y<=axeY*55+40){
            this.value = 2 + 5 * axeY + axeX;
            this.draw(this.value);
        }
    }
}



function defineMahjong(){
    console.log(valueMahjong);
}
