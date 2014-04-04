/* Manage the table of game */
/* Behave : build table, show new player (name, connected or not) */


var Table = {
    display:function(data){
        data.players.forEach(function(p){
            var pl = PlayerManager.getByOrientation(p.orientation);
            pl.setName(p.name);
        });
    },
    connectPlayer:function(orientation){
        PlayerManager.getByOrientation(orientation).drawing.setOnline();
    }


}

/* Display the name, the orientation and the status of the player */
function TitleBox(name,orientation){
    this.name = name;
    this.orientation = orientation;
    this.status = 0;    // 0 : OFFLINE, 1 : OFFLINE
    this.rotate = 0;
    this.color = '#FF0000';
    this.x = 0;this.y = 0;

    this.init = function(){
        switch(orientation){
            case "S" : this.x = 50;this.y = ComponentManager.variables.height - 80;break;
            case "O" : this.x = 80;this.y = 50;this.rotate = Math.PI/2;break;
            case "N" : this.x = ComponentManager.variables.width - 150;this.y = 30;break;
            case "E" : this.x = ComponentManager.variables.width - 80;this.y = ComponentManager.variables.height - 50;this.rotate = -Math.PI/2;break;
        }
    }

    this.draw = function(canvas){
        canvas.save();
        canvas.translate(this.x,this.y);
        canvas.rotate(this.rotate);
        canvas.strokeStyle=this.color;
        canvas.strokeRect(0,0,100,50);
        canvas.font = "14px Arial";
        canvas.fillText(this.name,50 - canvas.measureText(this.name).width/2,20);
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

    this.init();
}