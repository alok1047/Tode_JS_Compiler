/*
Expected:
1,2,3
3
1
2
*/
let a = [2];
a.push(3);
a.unshift(1);
console.log(a.join(','));
console.log(a.pop());
console.log(a.shift());
console.log(a.join(','));
