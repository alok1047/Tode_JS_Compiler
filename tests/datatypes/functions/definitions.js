/*
Expected:
1
2
3
*/
// declaration
function add(x, y) {
  return x + y;
}
console.log(add(0, 1));

// expression
const sub = function(x, y) {
  return x - y;
};
console.log(sub(5, 3));

// arrow function
const mul = (x, y) => x * y;
console.log(mul(1, 3));
