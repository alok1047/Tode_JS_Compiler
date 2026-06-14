/*
Expected:
1
2
default
1
2
*/
function testSwitch(x) {
  switch (x) {
    case 1:
      console.log("1");
      break;
    case 2:
      console.log("2");
      break;
    default:
      console.log("default");
  }
}
testSwitch(1);
testSwitch(2);
testSwitch(3);

// Fallthrough test
switch (1) {
  case 1:
    console.log("1");
  case 2:
    console.log("2");
    break;
}
