/*
Expected:
1
2
3
*/
let obj1 = { a: 1, b: 2 };
let obj2 = { ...obj1, c: 3 };
console.log(obj2.a);
console.log(obj2.b);
console.log(obj2.c);
