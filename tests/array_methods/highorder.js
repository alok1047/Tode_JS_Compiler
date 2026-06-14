/*
Expected:
3,6,9
1,3
20
20
1
true
true
*/
let a = [1, 2, 3];
console.log(a.map(x => x * 3).join(','));

let b = [1, 2, 3, 4];
console.log(b.filter(x => x % 2 !== 0).join(','));
console.log(b.reduce((sum, x) => sum + x, 10));

let c = [10, 20, 30];
console.log(c.find(x => x > 15));
console.log(c.findIndex(x => x > 15));

let d = [1, 2, 3];
console.log(d.some(x => x > 2));
console.log(d.every(x => x > 0));
