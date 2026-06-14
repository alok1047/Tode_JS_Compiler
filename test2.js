console.log("=== MEGA TEST START ===");

/* ------------------ OBJECT ACCESS ------------------ */
const key = "age";

const user = {
    name: "Alok",
    [key]: 20,

    greet() {
        return "Hello";
    }
};

console.log(user.name);
console.log(user["name"]);
console.log(user[key]);
console.log(user.greet());

delete user.age;
console.log(user.age);

/* ------------------ PROPERTY SHORTHAND ------------------ */
const city = "Lucknow";
const state = "UP";

const location = { city, state };

console.log(location.city);
console.log(location.state);

/* ------------------ OBJECT KEYS VALUES ENTRIES ------------------ */
const car = {
    brand: "Honda",
    year: 2024
};

console.log(Object.keys(car).join(","));
console.log(Object.values(car).join(","));

for (const [k, v] of Object.entries(car)) {
    console.log(k + ":" + v);
}

/* ------------------ FOR IN ------------------ */
for (const k in car) {
    console.log("forin-" + k);
}

/* ------------------ ARRAY DESTRUCTURING ------------------ */
const arr = [10, 20, 30, 40, 50];

const [a, b] = arr;
console.log(a);
console.log(b);

const [first, , third] = arr;
console.log(first);
console.log(third);

const [head, ...tail] = arr;
console.log(head);
console.log(tail.join(","));

/* ------------------ OBJECT DESTRUCTURING ------------------ */
const person = {
    username: "alok",
    role: "admin"
};

const { username } = person;
console.log(username);

const { username: userName } = person;
console.log(userName);

const { missing = "default" } = person;
console.log(missing);

/* ------------------ FOR OF ------------------ */
for (const x of [1, 2, 3]) {
    console.log("forof-" + x);
}

/* ------------------ ARRAY METHODS ------------------ */
const nums = [1, 2, 3];

nums.push(4);
console.log(nums.join(","));

nums.pop();
console.log(nums.join(","));

console.log(nums.includes(2));
console.log(nums.indexOf(3));

/* ------------------ SORT ------------------ */
const sortArr = [100, 2, 25, 1];

sortArr.sort((a, b) => a - b);
console.log(sortArr.join(","));

sortArr.sort((a, b) => b - a);
console.log(sortArr.join(","));

/* ------------------ FLAT ------------------ */
const nested = [1, [2, 3], [4, 5]];

console.log(nested.flat().join(","));

/* ------------------ SPREAD ARRAY ------------------ */
const spreadCopy = [...nums];
console.log(spreadCopy.join(","));

/* ------------------ SPREAD OBJECT ------------------ */
const obj1 = {
    a: 1,
    b: 2
};

const obj2 = {
    ...obj1,
    c: 3
};

console.log(obj2.a);
console.log(obj2.c);

/* ------------------ FUNCTION DECLARATION ------------------ */
console.log(add(5, 7));

function add(a, b) {
    return a + b;
}

/* ------------------ FUNCTION EXPRESSION ------------------ */
const sub = function (a, b) {
    return a - b;
};

console.log(sub(10, 3));

/* ------------------ DEFAULT PARAM ------------------ */
function greet(name = "Guest") {
    return name;
}

console.log(greet());
console.log(greet("Rohit"));

/* ------------------ REST PARAM ------------------ */
function sum(...nums) {
    let total = 0;

    for (const n of nums) {
        total += n;
    }

    return total;
}

console.log(sum(1, 2, 3, 4));

/* ------------------ CALLBACK ------------------ */
function calculator(a, b, operation) {
    return operation(a, b);
}

console.log(
    calculator(10, 20, function (x, y) {
        return x + y;
    })
);

/* ------------------ ARROW FUNCTION ------------------ */
const square = x => x * x;

console.log(square(6));

const makeUser = name => ({ name });

console.log(makeUser("Alok").name);

/* ------------------ MAP ------------------ */
const mapped = [1, 2, 3].map(x => x * 2);
console.log(mapped.join(","));

/* ------------------ FILTER ------------------ */
const filtered = [1, 2, 3, 4].filter(x => x % 2 === 0);
console.log(filtered.join(","));

/* ------------------ REDUCE ------------------ */
const reduced = [1, 2, 3, 4].reduce((acc, cur) => acc + cur, 0);
console.log(reduced);

/* ------------------ FIND ------------------ */
const found = [5, 10, 15].find(x => x === 10);
console.log(found);

/* ------------------ SOME ------------------ */
console.log(
    [1, 2, 3].some(x => x > 2)
);

/* ------------------ EVERY ------------------ */
console.log(
    [2, 4, 6].every(x => x % 2 === 0)
);

/* ------------------ DATE ------------------ */
const d = new Date(2025, 5, 14);

console.log(typeof Date.now());

console.log(d.getFullYear());
console.log(d.getMonth());
console.log(d.getDate());

d.setFullYear(2030);
console.log(d.getFullYear());

console.log(typeof d.toString());
console.log(typeof d.toDateString());

/* ------------------ SET ------------------ */
const s = new Set();

s.add(1);
s.add(2);
s.add(2);

console.log(s.has(1));
console.log(s.size);

s.delete(1);

console.log(s.has(1));

for (const v of s) {
    console.log(v);
}

/* ------------------ MAP OBJECT ------------------ */
const m = new Map();

m.set("name", "Alok");
m.set("age", 20);

console.log(m.get("name"));
console.log(m.has("age"));
console.log(m.size);

for (const [k, v] of m) {
    console.log(k + "=" + v);
}

console.log("=== ALL TESTS COMPLETED ===");