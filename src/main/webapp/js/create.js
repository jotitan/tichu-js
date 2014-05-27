function create(){
    var params = {};
    $(':text[name]:visible,:password[name]:visible','#idCreatePartie').each(function(i){
        params[$(this).attr('name')] = $(this).val();
    });

    var url = location.origin + location.pathname.substr(0,location.pathname.substr(1).indexOf("/")+1);
    url += '/rest/game/create';
    $.ajax({url:url,data:params,type:'GET',dataType:'jsonp',success:function(data){
      if(data.status == 1){
        $('#idMessageInfo').html('Game created').removeClass("alert-danger").addClass('alert-success');
      }else{
        $('#idMessageInfo').html('Error when creating the game : ' + data.message).removeClass("alert-success").addClass('alert-danger');
      }
    }});
}