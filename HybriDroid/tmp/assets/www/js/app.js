var app = {
'start': function(){
var a = bridge.getName();
var name = bridge.findName();
bridge.sendName("haha");
bridge.sendName(1,"rrr");
bridge.sendName();
document.getElementById('box').innerHtml = "hi";
	}
};

app.start();