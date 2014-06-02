/* Display informations */

var MessageInfo = {
    div:null,
    init:function(id){
        this.div = $('#' + id);
    },
    clean:function(){
        this.div.empty().removeClass();
    },
    _cleanClass:function(){
      this.div.removeClass().addClass('alert');
    },
    info:function(){
        this._cleanClass();
        this.div.html(message).addClass('alert-info');
    },
    success:function(message){
        this._cleanClass();
        this.div.html(message).addClass('alert-success');
    },
    fail:function(message){
        this._cleanClass();
        this.div.html(message).addClass('alert-danger');
    }
}