function foo(){
	console.log("aaa");
};

var app = {
	sendName: function(){
		var msgBox = document.getElementById("msgbox");
		var name = bridge.getName();
		foo();
		msgBox.innerHTML = "name is "+name;
		bridge.sendName(name+" Sungho");
	}
};

app.sendName();