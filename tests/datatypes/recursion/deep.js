/*
Expected:
500
*/
function recurse(n) {
  if (n === 0) return 0;
  return 1 + recurse(n - 1);
}
console.log(recurse(500));
