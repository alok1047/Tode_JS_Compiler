/*
Expected:
5
6
7
7
6
5
11
12
*/
let x = 5;
console.log(x++);
console.log(x);
console.log(++x);
console.log(x--);
console.log(x);
console.log(--x);

let freq = { count: 10 };
freq.count++;
console.log(freq.count);

let key = "count";
freq[key]++;
console.log(freq[key]);
