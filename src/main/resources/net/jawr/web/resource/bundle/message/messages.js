(function(){	
function r(val, args){
	for(var x = 0;x<args.length;x++){
		val = val.replace('{'+(x+1)+'}', args[x]);
	}
	return val;	
}
var regx = /({\d})/g;
function p() {
	var val = arguments[0];	
	var ret;
	if(regx.test(val))
		ret = function(){return r(val,arguments);}
	else ret = function(){return val;}
	for(var x = 1; x < arguments.length;x++) {
		for(var a in arguments[x])
			ret[a] = arguments[x][a];
	}
	return ret;
}
window.@namespace=(
@messages
)
})()