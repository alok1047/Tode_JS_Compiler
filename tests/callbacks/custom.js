/*
Expected:
before
executing callback with: hello
after
*/
function runWithCallback(cb, val) {
  console.log("before");
  cb(val);
  console.log("after");
}

runWithCallback((x) => {
  console.log("executing callback with: " + x);
}, "hello");
