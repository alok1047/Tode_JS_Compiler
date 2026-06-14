/*
Expected:
2,3
1,8,9,4
1,8,9,4,5,6
true
1
3
3-2-1
2,4,10,30
*/
let a = [1, 2, 3, 4];
console.log(a.slice(1, 3).join(','));
a.splice(1, 2, 8, 9);
console.log(a.join(','));
console.log(a.concat([5, 6]).join(','));

let b = [1, 2, 3, 2];
console.log(b.includes(2));
console.log(b.indexOf(2));
console.log(b.lastIndexOf(2));

let c = [1, 2, 3];
c.reverse();
console.log(c.join('-'));

let d = [10, 2, 30, 4];
d.sort((x, y) => x - y);
console.log(d.join(','));
