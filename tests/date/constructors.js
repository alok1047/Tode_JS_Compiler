/*
Expected:
0
1000
2025
5
13
*/
console.log(new Date(0).getTime());
console.log(new Date(1000).getTime());
let d = new Date(2025, 5, 13);
console.log(d.getFullYear());
console.log(d.getMonth());
console.log(d.getDate());
