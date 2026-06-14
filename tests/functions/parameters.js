/*
Expected:
15
11
30
*/
function addWithDefault(a, b = 10) {
  return a + b;
}
console.log(addWithDefault(5));
console.log(addWithDefault(5, 6));

function sumRest(...args) {
  let total = 0;
  for (let i = 0; i < args.length; i++) {
    total += args[i];
  }
  return total;
}
console.log(sumRest(10, 20));
