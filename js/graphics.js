/* Manage graphics component */

var ComponentManager = {
	ID_COMPONENT : 0,
	components:[],
	interval:null,
	canvas:null,
    variables:{
        width:700,height:500
    },
	run:function(){
	    this.canvas = $('#canvas').get(0).getContext('2d');
	    this.interval = setInterval(function(){ComponentManager.refresh();},50);
	},
	refresh:function(){
        canvas.clearRect(0,0,this.variables.width,this.variables.height);
        canvas.fillStyle='#00723D';
        canvas.fillRect(0,0,this.variables.width,this.variables.height);
        canvas.fillStyle='#000000';

        ComponentManager.components.forEach(function(c){
            c.draw(canvas);
        });

        CardManager.cards.forEach(function(c){
            c.drawing.draw(canvas);
        });
	},
	add:function(component){
		this.components.push(component);
	},
	remove:function(component){
		var index = this.components.indexOf(component);
		if(index!=-1){
			this.components.splice(index,1);
		}
	},
	getNextId:function(){
        return this.ID_COMPONENT++;
	}
}



function Component(){
	this.id = ComponentManager.getNextId();
	this.draw = function(){
		console.log("Not implemented");
	}
}

/* Box to drop a card */
function BoxCard(x,y,width,height){
	this.card = null;
	this.x = x;
	this.y = y;
	this.width = width;
	this.height = height;

	this.draw = function(canvas){
		canvas.strokeStyle = '#000000';
		canvas.fillStyle = '#FFFFFF';
		canvas.fillRect(x,y,width,height);
		canvas.strokeRect(x,y,width,height);
	}

	this.contains = function(x,y){
		return x >= this.x && x<= this.x + this.width
				&& y >= this.y && y<= this.y + this.height ;
	}
}


function drawCard(canvas,x,y,pos,checked,width,height,value,img,orientation){
	canvas.save();
	canvas.translate(x,y);
	var rotate = 0;
	var decalage = checked ? -10 : 0;
	switch(orientation){
		case "N" : rotate = Math.PI;break;
		case "S" : rotate = 0;break;
		case "O" : rotate = Math.PI/2;break;
		case "E" : rotate = -Math.PI/2;break;
	}
	canvas.rotate(rotate);
	canvas.fillStyle="#FFFFFF";
	canvas.fillRect(pos,decalage,width,height);
	canvas.strokeStyle="#000000";
	canvas.lineWidth=1;
	canvas.strokeRect(pos,decalage,width,height);
	if(img){
		canvas.drawImage(img,pos,decalage,width,height);
	}else{
		canvas.fillStyle=value.color;
		canvas.font = "6pt Arial";
		var marginRight = canvas.measureText(value.val).width + 3;
		canvas.fillText(value.val,pos + 2,10 + decalage);
		canvas.fillText(value.val,pos + width-marginRight,10 + decalage);
		canvas.fillText(value.val,pos + 2,height -5+ decalage);
		canvas.fillText(value.val,pos + width-marginRight,height -5 + decalage);
	}
	canvas.restore();
}