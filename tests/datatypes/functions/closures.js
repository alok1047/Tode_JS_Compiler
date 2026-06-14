/*
Expected:
15
20
*/
function f() {
  let x = 10;
  return () => {
    x += 5;
    return x;
  };
}
let g = f();
console.log(g());
console.log(g());
