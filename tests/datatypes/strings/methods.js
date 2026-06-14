/*
Expected:
5
HELLO
hello
hello
hello 
 hello
a|b|c
hi hello
hi hi
script
script
4
true
true
true
*/
let s = 'Hello';
console.log(s.length);
console.log(s.toUpperCase());
console.log(s.toLowerCase());

let s2 = ' hello ';
console.log(s2.trim());
console.log(s2.trimStart());
console.log(s2.trimEnd());

let s3 = 'a,b,c';
console.log(s3.split(',').join('|'));

let s4 = 'hello hello';
console.log(s4.replace('hello', 'hi'));
console.log(s4.replaceAll('hello', 'hi'));

let s5 = 'javascript';
console.log(s5.substring(4, 10));
console.log(s5.slice(-6));

let s6 = 'hello world';
console.log(s6.indexOf('o'));
console.log(s6.includes('world'));
console.log(s6.startsWith('he'));
console.log(s6.endsWith('ld'));
