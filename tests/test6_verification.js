console.log(typeof new Date());
let d = new Date("2025-06-13");
console.log(d.getFullYear());
console.log(Date.now() > 1700000000000); // verify Date.now() returns valid timestamp
console.log(new Date(0).toISOString());
let x = new Date();
x.setFullYear(2030);
console.log(x.getFullYear());
