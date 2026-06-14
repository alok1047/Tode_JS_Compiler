/*
Expected:
positive even
zero
negative
*/
function check(x) {
  if (x > 0) {
    if (x % 2 === 0) {
      console.log("positive even");
    } else {
      console.log("positive odd");
    }
  } else if (x === 0) {
    console.log("zero");
  } else {
    console.log("negative");
  }
}
check(4);
check(0);
check(-3);
