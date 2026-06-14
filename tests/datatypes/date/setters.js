/*
Expected:
2030
8
20
15
45
10
500
10000
*/
let d = new Date(0);
d.setFullYear(2030);
console.log(d.getFullYear());

d.setMonth(8);
console.log(d.getMonth());

d.setDate(20);
console.log(d.getDate());

d.setHours(15);
console.log(d.getHours());

d.setMinutes(45);
console.log(d.getMinutes());

d.setSeconds(10);
console.log(d.getSeconds());

d.setMilliseconds(500);
console.log(d.getMilliseconds());

d.setTime(10000);
console.log(d.getTime());
