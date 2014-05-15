var GameConnect = {
    div : null,
    selected : null,
    errorDiv:null,
    init:function(){
        this.div = $('#selectGame');
        this.errorDiv = $('#idErrorMessage');
        $('#idLoadGame').click(function(){
            GameInfo.loadGame($('#idGameName').val(),function(data){GameConnect.loadGame(data);});
        });
        this.div.dialog({
           title:'Connection',
            modal:true,
            width:600,
            buttons:[{
             text:"Connect",
             click:function(){
                if(!selected){
                    alert("Impossible");
                    return;
                }
                GameConnection.connect($('#idGameName').val(),this.selected.data.name,$('#idRename').val(),
                    function(){GameConnect.close();},
                    function(error){GameConnect.showError(error)});
             }}]
        });
    },
    showError:function(message){
        this.errorDiv.html(message);
    },
    close:function(){
        this.div.dialog('close');
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
            data.players.forEach(function(j){
               $('.joueur_' + j.orientation,this.div).bind('click',function(){
                    if(this.selected){
                        this.selected.div.removeClass('select_box');
                    }
                    this.selected = {div:$(this),data:j};
                    $('#idRename').val(j.name);
                    this.selected.div.addClass('select_box');
               }).find('span').html(j.name);
                if(j.connected){
                    $('.joueur_' + j.orientation,this.div).addClass('connected');
                }
            });
        }
    }
}

