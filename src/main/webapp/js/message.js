/* Display informations */

var MessageInfo = {
    div:null,
    init:function(id){
        this.div = $('#' + id);
    },
    clean:function(){
        this.div.empty().removeClass();
    },
    success:function(message){
        this.div.html(message).removeClass('alert-danger').addClass('alert-success');
    },
    fail:function(message){
        this.div.html(message).removeClass('alert-success').addClass('alert-danger');
    }
}