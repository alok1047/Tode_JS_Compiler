/*
Expected Error:
TypeError
*/
function third() {
  let a = undefined;
  let b = a.foo;
}

function second() {
  third();
}

function first() {
  second();
}

first();
