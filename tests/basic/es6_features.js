/*
Expected:
--- ITERATION ---
for...of array: 1 2 3
for...in object: a:1 b:2
for...of keys: a b
for...of values: 1 2
for...of entries: a:1 b:2
destructuring in loop: key=a val=1 key=b val=2

--- OBJECT FEATURES ---
shorthand: Alok, greet: hi
dynamic access: Civic
computed: hello: world
delete: undefined
hasOwnProperty name: true
hasOwnProperty age: false

--- ARRAY FEATURES ---
sparse array length: 101, val: 5
array hole length: 3, val: undefined
auto expansion length: 51, val: 10
lastIndexOf: 3
join: 1-2-3
flat: 1,2,3,4,5
spread copy: 1 2 3

--- DESTRUCTURING ---
array destructuring: 1 2
skipped: 1 3
rest elements: 1 [2,3]
object destructuring: Alok
renamed: Alok
default values: User Guest
nested: true
object rest: Alok {age:30,role:"User"}

--- FUNCTION FEATURES ---
hoisting: true
expression: 3
default params: Guest
rest params: 10
callbacks: 30

--- ARROW FUNCTIONS ---
implicit return: 16
returning object: Alok

--- DATE FEATURES ---
now: true
getters: true
setters: 2028-11-20
formatting: true

--- SEMANTICS ---
object ref: true
array ref: true
shallow copy: true
proper undefined handling: true
property lookup: true
prototype safe Object.keys: true
*/

console.log("--- ITERATION ---");
const arr = [1, 2, 3];
let outOf = "";
for (const x of arr) {
  outOf += x + " ";
}
console.log("for...of array: " + outOf.trim());

const obj = { a: 1, b: 2 };
let outIn = "";
for (const key in obj) {
  outIn += key + ":" + obj[key] + " ";
}
console.log("for...in object: " + outIn.trim());

let outKeys = "";
for (const key of Object.keys(obj)) {
  outKeys += key + " ";
}
console.log("for...of keys: " + outKeys.trim());

let outVals = "";
for (const val of Object.values(obj)) {
  outVals += val + " ";
}
console.log("for...of values: " + outVals.trim());

let outEntries = "";
for (const [k, v] of Object.entries(obj)) {
  outEntries += k + ":" + v + " ";
}
console.log("for...of entries: " + outEntries.trim());

let outDest = "";
for (const [k, v] of Object.entries(obj)) {
  outDest += "key=" + k + " val=" + v + " ";
}
console.log("destructuring in loop: " + outDest.trim());


console.log("\n--- OBJECT FEATURES ---");
const name = "Alok";
const greetObj = {
  name,
  greet() {
    return "hi";
  }
};
console.log("shorthand: " + greetObj.name + ", greet: " + greetObj.greet());

const key = "model";
const car = {
  [key]: "Civic"
};
console.log("dynamic access: " + car[key]);

const dynamicKey = "hello";
const computedObj = {
  [dynamicKey]: "world"
};
console.log("computed: " + dynamicKey + ": " + computedObj.hello);

const deleteObj = { name: "Alok" };
delete deleteObj.name;
console.log("delete: " + deleteObj.name);

const hasPropObj = { name: "Alok" };
console.log("hasOwnProperty name: " + hasPropObj.hasOwnProperty("name"));
console.log("hasOwnProperty age: " + hasPropObj.hasOwnProperty("age"));


console.log("\n--- ARRAY FEATURES ---");
const sparseArr = [];
sparseArr[100] = 5;
console.log("sparse array length: " + sparseArr.length + ", val: " + sparseArr[100]);

const holeArr = [1, , 3];
console.log("array hole length: " + holeArr.length + ", val: " + holeArr[1]);

const autoArr = [];
autoArr[50] = 10;
console.log("auto expansion length: " + autoArr.length + ", val: " + autoArr[50]);

console.log("lastIndexOf: " + [1, 2, 3, 2].lastIndexOf(2));
console.log("join: " + [1, 2, 3].join("-"));
console.log("flat: " + [1, [2, 3], [[4, 5]]].flat(2).join(","));

const spreadCopy = [...arr];
let outCopy = "";
for (const x of spreadCopy) {
  outCopy += x + " ";
}
console.log("spread copy: " + outCopy.trim());


console.log("\n--- DESTRUCTURING ---");
const [a, b] = arr;
console.log("array destructuring: " + a + " " + b);

const [x1, , x2] = arr;
console.log("skipped: " + x1 + " " + x2);

const [first, ...rest] = arr;
console.log("rest elements: " + first + " [" + rest.join(",") + "]");

const user = { name: "Alok", age: 30, role: "User" };
const { name: userName } = user;
console.log("object destructuring: " + userName);

const { name: renName } = user;
console.log("renamed: " + renName);

const { role = "User", nonExistent = "Guest" } = user;
console.log("default values: " + role + " " + nonExistent);

const nestedUser = { profile: { isAdmin: true } };
const { profile: { isAdmin } } = nestedUser;
console.log("nested: " + isAdmin);

const { name: rName, ...rRest } = user;
console.log("object rest: " + rName + " {age:" + rRest.age + ",role:\"" + rRest.role + "\"}");


console.log("\n--- FUNCTION FEATURES ---");
hoistedFn();
function hoistedFn() {
  console.log("hoisting: true");
}

const addFn = function(p1, p2) {
  return p1 + p2;
};
console.log("expression: " + addFn(1, 2));

function greetFn(pName = "Guest") {
  return pName;
}
console.log("default params: " + greetFn());

function sumRest(...nums) {
  let s = 0;
  for (const n of nums) s += n;
  return s;
}
console.log("rest params: " + sumRest(1, 2, 3, 4));

function runCallback(cb) {
  return cb(10, 20);
}
console.log("callbacks: " + runCallback((p1, p2) => p1 + p2));


console.log("\n--- ARROW FUNCTIONS ---");
const square = x => x * x;
console.log("implicit return: " + square(4));

const makeUser = nameVal => ({ name: nameVal });
console.log("returning object: " + makeUser("Alok").name);


console.log("\n--- DATE FEATURES ---");
console.log("now: " + (Date.now() > 0));
const d = new Date(1771123456789);
console.log("getters: " + (d.getFullYear() > 0 && d.getMonth() >= 0 && d.getDate() > 0));

d.setFullYear(2028);
d.setMonth(10);
d.setDate(20);
console.log("setters: " + d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate());

const formattingOk = d.toString() !== "" && d.toDateString() !== "" && d.toISOString() !== "" && d.toLocaleString() !== "";
console.log("formatting: " + formattingOk);


console.log("\n--- SEMANTICS ---");
const ref1 = {};
const ref2 = ref1;
ref2.x = 10;
console.log("object ref: " + (ref1.x === 10));

const arrRef1 = [];
const arrRef2 = arrRef1;
arrRef2.push(5);
console.log("array ref: " + (arrRef1[0] === 5));

const shallowOrig = { a: 1, b: { c: 2 } };
const shallowCopyObj = { ...shallowOrig };
shallowCopyObj.a = 10;
shallowCopyObj.b.c = 20;
console.log("shallow copy: " + (shallowOrig.a === 1 && shallowOrig.b.c === 20));

let undefVar;
console.log("proper undefined handling: " + (undefVar === undefined));

const protoObj = { a: 1 };
console.log("property lookup: " + (protoObj.toString() !== undefined));

const checkKeys = Object.keys({ a: 1 });
console.log("prototype safe Object.keys: " + (checkKeys.length === 1 && checkKeys[0] === "a"));
