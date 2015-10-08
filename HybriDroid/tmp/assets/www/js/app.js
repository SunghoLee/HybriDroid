var app = {
load: function(){
	app.setName();	
},
setName: function(){
var box = document.getElementById("box");
if(typeof box === 'undefined')
	console.log("why?");
else
	box.innerText = bridge.getName();
}
};