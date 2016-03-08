function foo5(){
	console.log(bridge1.getNumber());
	foo6();
};

function foo6(){
	console.log(bridge1.getSchool());
};

foo5();