/*
Expected:
0
1
true
true
1000
*/
let empty = "";
console.log(empty.length);

let space = " ";
console.log(space.length);
console.log(empty.trim() === "");
console.log(space.trim() === "");

// A moderately long string test
let longStr = "";
for (let i = 0; i < 100; i++) {
  longStr = longStr + "1234567890";
}
console.log(longStr.length);
