function foo3(){
	console.log(bridge1.getAddress());
	foo4();
};

function foo4(){
	console.log(bridge1.getName());
};

foo3();