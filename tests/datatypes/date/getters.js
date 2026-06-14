/*
Expected:
2025
5
13
10
30
45
123
5
*/
let d = new Date(2025, 5, 13, 10, 30, 45, 123);
console.log(d.getFullYear());
console.log(d.getMonth());
console.log(d.getDate());
console.log(d.getHours());
console.log(d.getMinutes());
console.log(d.getSeconds());
console.log(d.getMilliseconds());
console.log(d.getDay()); // June 13, 2025 is Friday (5)
