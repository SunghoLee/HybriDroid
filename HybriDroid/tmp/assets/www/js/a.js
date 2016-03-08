function foo1(){
	console.log(bridge1.getName());
	foo2();
};

function foo2(){
	console.log(bridge1.getAddress());
};

foo1();