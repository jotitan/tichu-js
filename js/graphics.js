/* Manage graphics component */

var ComponentManager = {
	ID_COMPONENT : 0,
	components:[],
	interval:null,
	canvas:null,
    variables:{
        width:600,height:400
    },
	run:function(){
	    this.canvas = $('#canvas').get(0).getContext('2d');
	    this.interval = setInterval(function(){ComponentManager.refresh();},50);
	},
	refresh:function(){
        this.canvas.clearRect(0,0,this.variables.width,this.variables.height);
        this.canvas.fillStyle='#00723D';
        this.canvas.fillRect(0,0,this.variables.width,this.variables.height);
        this.canvas.fillStyle='#000000';

        ComponentManager.components.forEach(function(c){
            c.draw(this.canvas);
        },this);

        CardManager.cards.forEach(function(c){
            c.drawing.draw(this.canvas);
        },this);
        CardManager.emptyCards.forEach(function(c){
            c.drawing.draw(this.canvas);
        },this);
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
		case "O" : rotate = Math.PI/2;break;
		case "N" : rotate = Math.PI;break;
		case "E" : rotate = -Math.PI/2;break;
		case "S" : rotate = 0;break;
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

/* Display the name, the orientation and the status of the player */
function TitleBox(name,orientation){
    Component.call(this);
    this.name = name;
    this.orientation = orientation;
    this.status = 0;    // 0 : OFFLINE, 1 : ONLINE
    this.rotate = 0;
    this.color = '#FF0000';
    this.x = 0;this.y = 0;
    this.select = false;
    this.annonce = null;

    this.init = function(){
        switch(this.orientation){
            case "S" : this.x = 20;this.y = ComponentManager.variables.height - 70;this.rotate=0;break;
            case "O" : this.x = 70;this.y = 20;this.rotate = Math.PI/2;break;
            case "N" : this.x = ComponentManager.variables.width - 120;this.y = 20;this.rotate=0;break;
            case "E" : this.x = ComponentManager.variables.width - 70;this.y = ComponentManager.variables.height - 20;this.rotate = -Math.PI/2;break;
        }
    }

    this.draw = function(canvas){
        this.init();
        canvas.save();
        canvas.translate(this.x,this.y);
        canvas.rotate(this.rotate);
        canvas.strokeStyle=this.color;
        canvas.lineWidth = this.select ? 5 : 1;
        canvas.strokeRect(0,0,100,50);
        canvas.font = (this.select ? "small-caps bold ":"") +  "14px Arial";
        canvas.fillStyle = this.color;
        var name = this.name + ((this.annonce!=null)?' - ' + this.annonce:'');
        canvas.fillText(name,50 - canvas.measureText(name).width/2,20);
        canvas.restore();
    }

    this.setOnline = function(){
        this.color = '#00FF00';
    }

    this.setOffline = function(){
        this.color = '#FF0000';
    }

    this.setName = function(name){
        this.name = name;
    }

    this.changeOrientation = function(orientation){
        this.orientation = orientation;
        this.init();
    }

    this.init();
}