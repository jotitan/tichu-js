var GameConnect = {
    div : null,
    selected : null,
    init:function(){
        this.div = $('#connectModal');
        $('#idLoadGame').click(function(){
            GameInfo.loadGame($('#idGameName').val(),function(data){GameConnect.loadGame(data);});
        });
        $('#idConnectGame').bind('click',function(){GameConnect._clickConnect();});
        $('#idRefresh').bind('click',function(){
           GameConnect.refreshList();
        });
        this.refreshList();
    },
    setAndLoad:function(name){
        $('#idGameName').val(name);
        $('#idLoadGame').click();
    },
    delete:function(game){

    },
    _clickConnect:function(){
        if(!this.selected){
            alert("Impossible");
            return;
        }
        GameConnection.connect($('#idGameName').val(),this.selected.data.name,$('#idRename').val(),
            function(){GameConnect.close();},
            function(error){MessageInfo.fail(error)});
    },
    close:function(){
        $('#idPlayTab').show().find('a').click();
        $('#connectModal').modal('hide');
        MessageInfo.clean();
    },
    loadGame:function(data){
        if(data.message){
            MessageInfo.fail(data.message)
            $('span[class*="joueur_"] > span',this.div).unbind('click').html('');
        }else{
            $('#idJoin > a').click();
            if(data.password){
                $('#idPass').show();
            }else{
                $('#idPass').hide();
            }
            var nb = 4;
            data.players.forEach(function(j){
               $('.joueur_' + j.orientation,this.div)
               .bind('click',function(){
                    GameConnect._clickName(j,this);
               })
               .bind('dblclick',function(){
                    GameConnect._clickName(j,this);
                    GameConnect._clickConnect();
               })
               .find('span').html(j.name);
                if(j.connected){
                    nb--;
                    $('.joueur_' + j.orientation,this.div).addClass('connected');
                }
            },this);
            MessageInfo.success("Game find, " + nb + " free chair");
            $('#connectModal').modal('show');
        }
    },
    _clickName:function(j,box){
        if(this.selected){
            this.selected.div.removeClass('select_box');
        }
        this.selected = {div:$(box),data:j};
        $('#idRename').val(j.name);
        this.selected.div.addClass('select_box');
    },
    refreshList:function(){
         GameInfo.listGames(function(data){
             $('#idListGame > tbody').empty();
             data.forEach(function(g,i){
                 var tr = $('<tr><td>' + i + '</td><td>' + g.game
                     + '</td><td>' + g.nb + '</td><td>'
                     + '<span class="join"><a href="#join">join</a> </span>'
                     + '<span class="delete"> <a href="#">delete</a></span>'
                     + '</td></tr>');
                 tr.find('span.join').click(function(e){
                     GameConnect.setAndLoad(g.game);
                     $('#connectModal').modal('show');
                 });
                 tr.find('span.delete').click(function(e){
                     GameConnect.delete(g.game);
                     tr.remove();
                 });
                 $('#idListGame').append(tr);
             });
         });
    }
}