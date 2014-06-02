function create(){
    var params = {};
    $(':text[name]:visible,:password[name]:visible','#idCreatePartie').each(function(i){
        params[$(this).attr('name')] = $(this).val();
    });
    $(':checkbox[name]','#idCreatePartie').each(function(){
        params[$(this).attr('name')] = $(this).is(':checked');
    });

    var url = location.origin + location.pathname.substr(0,location.pathname.substr(1).indexOf("/")+1);
    url += '/rest/game/create';
    $.ajax({url:url,data:params,type:'GET',dataType:'jsonp',success:function(data){
      if(data.status == 1){
          MessageInfo.success('Game created');
      }else{
          MessageInfo.fail('Error when creating the game : ' + data.message);
      }
    }});
}