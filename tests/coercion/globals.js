/*
Expected:
object
undefined
number
number
true
true
false
*/
console.log(typeof null);
console.log(typeof undefined);
console.log(typeof NaN);
console.log(typeof Infinity);
console.log(isNaN(NaN));
console.log(!isFinite(Infinity));
console.log(isFinite(NaN));
