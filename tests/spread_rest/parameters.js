/*
Expected:
3
6
6
*/
function sum(a, ...args) {
  let s = a;
  for (let i = 0; i < args.length; i++) {
    s += args[i];
  }
  return s;
}
console.log(sum(1, 2));
console.log(sum(1, 2, 3));

// Parameter spread / argument spread
let nums = [2, 3];
console.log(sum(1, ...nums));
