/* DONE : creer le jeu de cartes */
/* DONE : Creer carte speciale (image) */
/* DONE : cacher les cartes des autres joueurs */
/* DONE : gerer un mode recto et un mode verso */
/* TODO : Case pour l'echange au depart (seul mouvement libre) */
/* TODO : detecter les bombes */
/* TODO : validation des regles (par rapport au carte d'avant) */

function Card(value,color){
	this.value = value || 0;
	this.color = color || 'white';
	this.player = null;
	this.status = STATUS_CARD.NO_STATUS_CARD;
	
	this.getScore = function(){
		switch(this.value){
			case 5:return 5;
			case 10:return 10;
			case 13:return 10;
		}
		return 0;
	}	
	this.drawing = new DrawingCard(0,0,value,color);
	
	/* Position de la carte */
	this.setPlayer = function(player,pos){
		this.player = player;
		this.drawing.setOrientation(player.orientation,pos);
	}
	
	this.setStatus = function(status){
		this.status = status;
		this.drawing.status = status;
	}
	
	this.toString = function(){
		return value + " " + color;
	}
}

function MahjongCard(){
	Card.call(this,1);
	
	this.drawing = new ImgCard(0,0,"img/mahjong.png");	
}

function DogsCard(){
	Card.call(this,15);
	this.drawing = new ImgCard(0,0,"img/dogs.png");	
}

function PhoenixCard(){
	Card.call(this,16);
	this.drawing = new ImgCard(0,0,"img/phoenix.png");
	
	this.getScore = function(){return -25;}
}

function DragonCard(){
	Card.call(this,17);
	this.drawing = new ImgCard(0,0,"img/dragon.png");
	
	this.getScore = function(){return 25;}
}

function ImgCard(x,y,img){
	DrawingCard.call(this,x,y);
	this.img = new Image();
	this.img.src = img;
	
	this._drawCard = function(canvas){
		drawCard(canvas,this.x,this.y,this.pos,this.checked,this.width,this.height,null,this.img,this.orientation);
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
		canvas.fillText(value.val,pos + 2,10 + decalage);
		canvas.fillText(value.val,pos + width-7,10 + decalage);
		canvas.fillText(value.val,pos + 2,35 + decalage);
		canvas.fillText(value.val,pos + width-7,35 + decalage);
	}
	canvas.restore();
}

function DrawingCard(x,y,value,color){
	this.x = x;
	this.y = y;
	this.pos = 0;
	this.width = 25;
	this.height = 40;
	this.value = null;
	this.color = color;
	this.recto = false;
	this.imgRecto = new Image();
	this.imgRecto.src='img/recto.png';
	this.checked = false;
	this.deep = 0;
	
	switch(value){
		case 11 :this.value='V';break;
		case 12 :this.value='D';break;
		case 13 :this.value='K';break;
		case 14 :this.value='A';break;
		default : this.value = value;
	}

	this.setOrientation = function(orientation,pos,nb){		
		this.orientation = orientation;
		switch(orientation){
			case "C" : this.x = 150 + nb*5;this.y = 80 + nb*15;break;
			case "N" : this.x = 400;this.y = 50;break;
			case "S" : this.x = 100;this.y = 200;break;
			case "O" : this.x = 70;this.y = 50;break;
			case "E" : this.x = 450;this.y = 200;break;
		}
		this.setPosition(pos);
	}
	
	this.setPosition = function(pos){
		this.pos = pos*12;
	}

	this.draw = function(canvas){
		/* Case when displayed */
		if(this.status !=STATUS_CARD.DISTRIBUTED_CARD && this.status != STATUS_CARD.TABLE_CARD){return;}
		
		if(this.recto){
			return this.drawRecto(canvas);
		}
		this._drawCard(canvas);
	}
	
	this._drawCard = function(canvas){
		drawCard(canvas,this.x,this.y,this.pos,this.checked,this.width,this.height,{val:this.value,color:this.color},null,this.orientation);		
	}
	
	this.drawRecto = function(canvas){
		drawCard(canvas,this.x,this.y,this.pos,this.checked,this.width,this.height,null,this.imgRecto,this.orientation);		
	}
	
	this.contains = function(x,y){
		return x>=this.x && x <= this.x+this.width && y>=this.y && y<=this.y + this.height;
	}
}

/* Manage cards actually played */
var Plateau = {
	turn:0,		// Turn of game
	folds:[],	// List of folds on table
	playFold:function(cards,player){
		// Verify the fold
		
		// Sort cards to represent combination
		// Show fold
		cards.forEach(function(c,i){
			c.setStatus(STATUS_CARD.TABLE_CARD);
			c.drawing.recto = false;
			c.drawing.checked = false;
			c.drawing.setOrientation("C",i,this.folds.length);
			c.drawing.deep = this.folds.length;
			player.playCard(c);
		},this);
		
		this.folds.push(cards);
		// Reorganize cards
		player.sortCards();
		CardManager.sortCards();
	},
	/* Check that combinaison is correct */
	checkCombinaison:function(cards){
		
	}

}