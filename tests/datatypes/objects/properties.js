/*
Expected:
1
2
42
10
20
a,b
1,2
99
*/
let o = { x: 1, y: 2 };
console.log(o.x);
console.log(o.y);

o.z = 42;
console.log(o.z);

let o2 = { a: 10 };
let k = 'a';
console.log(o2[k]);
o2[k] = 20;
console.log(o2.a);

let o3 = { a: 1, b: 2 };
console.log(Object.keys(o3).join(','));
console.log(Object.values(o3).join(','));

// Nested objects
let nested = { foo: { bar: 99 } };
console.log(nested.foo.bar);
