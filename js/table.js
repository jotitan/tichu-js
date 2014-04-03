/* Manage the table of game */
/* Behave : build table, show new player (name, connected or not) */


var Table = {

    load:function(name){
        $.ajax({
            url:'http://localhost:8081/tichu-server/rest/game/info/' + name,
            dataType:'jsonp',
            success:function(data){
               Table.display(data);
            }
        })
    },
    display:function(data){
        console.log(data);
    }

}

/* Display the name, the orientation and the status of the player */
function TitleBox(String name, String orientation){
    this.name = name;
    this.orientation = orientation;
    this.status = 0;    // 0 : OFFLINE, 1 : OFFLINE

}