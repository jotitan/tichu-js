/* Manage messages between server and clients */


var MessageManager = {
	GameConnection:{
		url:'http://localhost:8080/tichu-server/rest/game/join',
		/* Create connection to a party */
		connect:function(game,player){
			$.ajax({
				url:this.url,
				data:{game:game,name:player},
				dataType:'jsonp',
				success:function(data){
					if(data.status && data.status == 0){
						alert(data.message);
					}else{
						MessageManager.ServerConnect.init(data.token);
					}
				}
			})
		}
	},
	ServerConnect : {
		token:null,	// token give by server to communicate
		ws:null,
		url:"ws://localhost:8080/tichu-server/chat4",
		init:function(token){
			this.token = token;
			this.ws = new WebSocket(this.url + "?token=" + this.token);
			this.ws.onmessage = function(message){
				console.log("Receive : ",message);
				//MessageManager.token = ;
			}
			this.ws.onopen = function(message){
				console.log("Open : ",message);
			}
		},
		readMessage:function(message){
			try{
				var data = JSON.parse(message);
			}catch(e){
				console.log("NOT JSON : " + message);
			}
		},
		sendMessage:function(message){
			this.ws.send(message);
		},
		close:function(){
			this.ws.close();
		}
	}
	

}