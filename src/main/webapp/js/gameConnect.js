var GameConnect = {
    div : null,
    selected : null,
    errorDiv:null,
    init:function(){
        this.div = $('#join');
        this.errorDiv = $('#idMessageInfo');
        $('#idLoadGame').click(function(){
            GameInfo.loadGame($('#idGameName').val(),function(data){GameConnect.loadGame(data);});
        });
        $('#idConnectGame').bind('click',function(){GameConnect._clickConnect();});
        return;
        this.div.dialog({
           title:'Connection',
            modal:true,
            width:600,
            buttons:[{
             text:"Connect",
             click:function(){
                  GameConnect._clickConnect();
             }}]
        });
    },
    _clickConnect:function(){
        if(!this.selected){
            alert("Impossible");
            return;
        }
        GameConnection.connect($('#idGameName').val(),this.selected.data.name,$('#idRename').val(),
            function(){GameConnect.close();},
            function(error){GameConnect.showError(error)});
    },
    showError:function(message){
        this.errorDiv.html(message).removeClass('alert-success').addClass('alert-danger');
    },
    close:function(){
        $('#idPlayTab').show().find('a').click();
    },
    loadGame:function(data){
        if(data.message){
            this.showError(data.message)
            $('span[class*="joueur_"] > span',this.div).unbind('click').html('');
        }else{
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
            this.errorDiv.html("Game find, " + nb + " free chair").removeClass('alert-danger').addClass('alert-success');
        }
    },
    _clickName:function(j,box){
        if(this.selected){
            this.selected.div.removeClass('select_box');
        }
        this.selected = {div:$(box),data:j};
        $('#idRename').val(j.name);
        this.selected.div.addClass('select_box');
    }
}

