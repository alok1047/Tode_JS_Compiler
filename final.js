// ============================================================
// THUNDER HACKATHON — FULL COVERAGE TEST FILE
// Tests every feature from the hidden test case spec
// Run: java -cp src minijs.Main full_test.js
// ============================================================

// ─────────────────────────────────────────────
// 1. VARIABLE DECLARATIONS
// ─────────────────────────────────────────────
let x = 10;
const PI = 3.14159;
let name = "ThunderJS";
let nothing = null;
let undef = undefined;
let flag = true;

console.log(x);          // 10
console.log(PI);         // 3.14159
console.log(name);       // ThunderJS
console.log(nothing);    // null
console.log(undef);      // undefined
console.log(flag);       // true

// ─────────────────────────────────────────────
// 2. PRIMITIVE DATA TYPES
// ─────────────────────────────────────────────
console.log(typeof 42);           // number
console.log(typeof "hello");      // string
console.log(typeof true);         // boolean
console.log(typeof null);         // object   (classic JS quirk)
console.log(typeof undefined);    // undefined
console.log(typeof function(){}); // function

// ─────────────────────────────────────────────
// 3. ARITHMETIC, COMPARISON, LOGICAL, ASSIGNMENT OPERATORS
// ─────────────────────────────────────────────
let a = 15;
let b = 4;
console.log(a + b);   // 19
console.log(a - b);   // 11
console.log(a * b);   // 60
console.log(a / b);   // 3.75
console.log(a % b);   // 3
console.log(a ** b);  // 50625

// comparison
console.log(5 == "5");    // true   (loose)
console.log(5 === "5");   // false  (strict)
console.log(null == undefined);  // true
console.log(null === undefined); // false
console.log(NaN === NaN);        // false

// logical
console.log(true && false);  // false
console.log(true || false);  // true
console.log(!true);          // false

// assignment operators
let c = 10;
c += 5;  console.log(c);  // 15
c -= 3;  console.log(c);  // 12
c *= 2;  console.log(c);  // 24
c /= 4;  console.log(c);  // 6
c %= 4;  console.log(c);  // 2

// ternary
let age = 20;
let status = age >= 18 ? "Adult" : "Minor";
console.log(status); // Adult

// ─────────────────────────────────────────────
// 4. CONDITIONAL STATEMENTS
// ─────────────────────────────────────────────

// if / else if / else
let score = 75;
if (score >= 90) {
    console.log("Grade: A");
} else if (score >= 75) {
    console.log("Grade: B");  // Grade: B
} else if (score >= 60) {
    console.log("Grade: C");
} else {
    console.log("Grade: F");
}

// switch
let day = 3;
switch (day) {
    case 1: console.log("Monday"); break;
    case 2: console.log("Tuesday"); break;
    case 3: console.log("Wednesday"); break;  // Wednesday
    case 4: console.log("Thursday"); break;
    default: console.log("Other");
}

// ─────────────────────────────────────────────
// 5. LOOPS
// ─────────────────────────────────────────────

// for loop
let forSum = 0;
for (let i = 1; i <= 5; i++) {
    forSum += i;
}
console.log(forSum); // 15

// while loop
let whileCount = 1;
let whileProduct = 1;
while (whileCount <= 5) {
    whileProduct *= whileCount;
    whileCount++;
}
console.log(whileProduct); // 120

// do...while
let doVal = 0;
do {
    doVal += 10;
} while (doVal < 50);
console.log(doVal); // 50

// nested loops (triangle)
for (let i = 1; i <= 3; i++) {
    let row = "";
    for (let j = 1; j <= i; j++) {
        row += "*";
    }
    console.log(row);
}
// *
// **
// ***

// ─────────────────────────────────────────────
// 6. ARRAYS + COMMON OPERATIONS
// ─────────────────────────────────────────────
let arr = [3, 1, 4, 1, 5, 9, 2, 6];

arr.push(7);
console.log(arr[arr.length - 1]); // 7

arr.pop();
console.log(arr.length); // 8

arr.unshift(0);
console.log(arr[0]); // 0

arr.shift();
console.log(arr[0]); // 3

// slice (non-destructive)
let sliced = arr.slice(1, 4);
console.log(sliced.join(", ")); // 1, 4, 1

// splice (destructive)
let splicedArr = [10, 20, 30, 40, 50];
splicedArr.splice(1, 2);
console.log(splicedArr.join(", ")); // 10, 40, 50

// concat
let merged = [1, 2].concat([3, 4]);
console.log(merged.join(", ")); // 1, 2, 3, 4

// includes / indexOf
console.log([1, 2, 3].includes(2));   // true
console.log([1, 2, 3].indexOf(3));    // 2
console.log([1, 2, 3].includes(99));  // false

// sort
let nums = [5, 3, 8, 1, 2];
nums.sort((a, b) => a - b);
console.log(nums.join(", ")); // 1, 2, 3, 5, 8

// reverse
let rev = [1, 2, 3, 4, 5];
rev.reverse();
console.log(rev.join(", ")); // 5, 4, 3, 2, 1

// ─────────────────────────────────────────────
// 7. STRINGS + COMMON OPERATIONS
// ─────────────────────────────────────────────
let str = "  Hello, World!  ";

console.log(str.trim());                        // Hello, World!
console.log(str.trim().toUpperCase());          // HELLO, WORLD!
console.log(str.trim().toLowerCase());          // hello, world!
console.log(str.trim().includes("World"));      // true
console.log(str.trim().startsWith("Hello"));    // true
console.log(str.trim().endsWith("!"));          // true
console.log(str.trim().indexOf("World"));       // 7
console.log(str.trim().replace("World", "JS")); // Hello, JS!
console.log(str.trim().substring(0, 5));        // Hello
console.log(str.trim().slice(7, 12));           // World

let csv = "one,two,three";
let parts = csv.split(",");
console.log(parts.length);   // 3
console.log(parts[1]);       // two

let messy = "hello world hello";
console.log(messy.replaceAll("hello", "hi")); // hi world hi

// ─────────────────────────────────────────────
// 8. OBJECTS + MANIPULATION
// ─────────────────────────────────────────────
let person = {
    name: "Alok",
    age: 21,
    city: "Lucknow"
};

console.log(person.name);       // Alok
console.log(person["age"]);     // 21

person.email = "alok@mail.com";
console.log(person.email);      // alok@mail.com

person.age = 22;
console.log(person.age);        // 22

// nested object
let company = {
    name: "ThunderCorp",
    address: {
        city: "Delhi",
        pin: 110001
    }
};
console.log(company.address.city); // Delhi
console.log(company.address.pin);  // 110001

// Object.keys / values / entries
let keys = Object.keys(person);
console.log(keys.includes("name"));  // true
console.log(keys.includes("age"));   // true

// ─────────────────────────────────────────────
// 9. FUNCTIONS
// ─────────────────────────────────────────────

// function declaration
function add(a, b) {
    return a + b;
}
console.log(add(3, 4)); // 7

// function expression
const multiply = function(a, b) {
    return a * b;
};
console.log(multiply(3, 4)); // 12

// arrow function
const square = (n) => n * n;
console.log(square(5)); // 25

// arrow with block body
const greet = (name) => {
    let msg = "Hello, " + name + "!";
    return msg;
};
console.log(greet("World")); // Hello, World!

// default parameters
function power(base, exp = 2) {
    return base ** exp;
}
console.log(power(3));    // 9
console.log(power(2, 8)); // 256

// recursion
function factorial(n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);
}
console.log(factorial(5));  // 120
console.log(factorial(10)); // 3628800

// closures
function makeCounter() {
    let count = 0;
    return function() {
        count++;
        return count;
    };
}
const counter = makeCounter();
console.log(counter()); // 1
console.log(counter()); // 2
console.log(counter()); // 3

// ─────────────────────────────────────────────
// 10. CALLBACK FUNCTIONS
// ─────────────────────────────────────────────
function applyTwice(fn, value) {
    return fn(fn(value));
}
console.log(applyTwice(x => x * 2, 3)); // 12

function doMath(a, b, operation) {
    return operation(a, b);
}
console.log(doMath(10, 5, (a, b) => a - b)); // 5

// ─────────────────────────────────────────────
// 11. ARRAY METHODS (map, filter, reduce, find, some, every)
// ─────────────────────────────────────────────
let numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

// map
let doubled = numbers.map(n => n * 2);
console.log(doubled.join(", ")); // 2, 4, 6, 8, 10, 12, 14, 16, 18, 20

// filter
let evens = numbers.filter(n => n % 2 === 0);
console.log(evens.join(", ")); // 2, 4, 6, 8, 10

// reduce
let sum = numbers.reduce((acc, n) => acc + n, 0);
console.log(sum); // 55

// find
let firstOver5 = numbers.find(n => n > 5);
console.log(firstOver5); // 6

// some
console.log(numbers.some(n => n > 9));   // true
console.log(numbers.some(n => n > 99));  // false

// every
console.log(numbers.every(n => n > 0));  // true
console.log(numbers.every(n => n > 5));  // false

// chaining
let result = numbers
    .filter(n => n % 2 === 0)
    .map(n => n * n)
    .reduce((acc, n) => acc + n, 0);
console.log(result); // 220  (4+16+36+64+100)

// ─────────────────────────────────────────────
// 12. MATH OBJECT
// ─────────────────────────────────────────────
console.log(Math.abs(-42));       // 42
console.log(Math.floor(3.9));     // 3
console.log(Math.ceil(3.1));      // 4
console.log(Math.round(3.5));     // 4
console.log(Math.round(3.4));     // 3
console.log(Math.max(1, 5, 3));   // 5
console.log(Math.min(1, 5, 3));   // 1
console.log(Math.pow(2, 10));     // 1024
console.log(Math.sqrt(144));      // 12

// Math.random (just verify it's a number in [0,1))
let rand = Math.random();
console.log(rand >= 0 && rand < 1); // true

// ─────────────────────────────────────────────
// 13. DATE OBJECT
// ─────────────────────────────────────────────
let now = new Date();
console.log(typeof now.getFullYear() === "number"); // true
console.log(now.getFullYear() >= 2024);             // true
console.log(now.getMonth() >= 0 && now.getMonth() <= 11); // true
console.log(now.getDate() >= 1 && now.getDate() <= 31);   // true

// ─────────────────────────────────────────────
// 14. TYPE CONVERSION AND COERCION
// ─────────────────────────────────────────────

// explicit conversion
console.log(Number("42"));       // 42
console.log(Number("3.14"));     // 3.14
console.log(Number(true));       // 1
console.log(Number(false));      // 0
console.log(Number(null));       // 0
console.log(String(123));        // 123
console.log(String(true));       // true
console.log(Boolean(0));         // false
console.log(Boolean(""));        // false
console.log(Boolean(null));      // false
console.log(Boolean(undefined)); // false
console.log(Boolean(1));         // true
console.log(Boolean("hi"));      // true
console.log(Boolean([]));        // true

// implicit coercion
console.log("5" + 3);    // 53   (string concat)
console.log("5" - 3);    // 2    (numeric)
console.log("5" * 2);    // 10
console.log(true + 1);   // 2
console.log(false + 1);  // 1
console.log(null + 1);   // 1

// parseInt / parseFloat
console.log(parseInt("42px"));    // 42
console.log(parseFloat("3.14em")); // 3.14
console.log(parseInt("abc"));      // NaN
console.log(isNaN(NaN));           // true
console.log(isNaN("hello"));       // true
console.log(isNaN(42));            // false

// ─────────────────────────────────────────────
// 15. SPREAD AND REST OPERATORS
// ─────────────────────────────────────────────

// spread in array
let arr1 = [1, 2, 3];
let arr2 = [4, 5, 6];
let combined = [...arr1, ...arr2];
console.log(combined.join(", ")); // 1, 2, 3, 4, 5, 6

// spread in function call
console.log(Math.max(...arr1)); // 3

// spread copy (non-mutating)
let original = [1, 2, 3, 4, 5];
let copy = [...original];
copy.reverse();
console.log(original.join(", ")); // 1, 2, 3, 4, 5  (unchanged)
console.log(copy.join(", "));     // 5, 4, 3, 2, 1

// rest parameters
function sumAll(...args) {
    return args.reduce((acc, n) => acc + n, 0);
}
console.log(sumAll(1, 2, 3));          // 6
console.log(sumAll(10, 20, 30, 40));   // 100

// spread in object
let base = { a: 1, b: 2 };
let extended = { ...base, c: 3 };
console.log(extended.a); // 1
console.log(extended.c); // 3

// ─────────────────────────────────────────────
// 16. REAL-WORLD COMBO PATTERNS
//     (what hidden tests likely probe)
// ─────────────────────────────────────────────

// FizzBuzz (classic hidden test variant)
for (let i = 1; i <= 15; i++) {
    if (i % 15 === 0) console.log("FizzBuzz");
    else if (i % 3 === 0) console.log("Fizz");
    else if (i % 5 === 0) console.log("Buzz");
    else console.log(i);
}

// Fibonacci sequence
function fibonacci(n) {
    if (n <= 0) return 0;
    if (n === 1) return 1;
    return fibonacci(n - 1) + fibonacci(n - 2);
}
console.log(fibonacci(7));  // 13
console.log(fibonacci(10)); // 55

// Prime checker
function isPrime(n) {
    if (n < 2) return false;
    for (let i = 2; i <= Math.sqrt(n); i++) {
        if (n % i === 0) return false;
    }
    return true;
}
console.log(isPrime(17)); // true
console.log(isPrime(18)); // false

// Word frequency counter (objects + strings + loops)
let sentence = "the cat sat on the mat the cat";
let words = sentence.split(" ");
let freq = {};
for (let i = 0; i < words.length; i++) {
    let word = words[i];
    if (freq[word]) {
        freq[word]++;
    } else {
        freq[word] = 1;
    }
}
console.log(freq["the"]);  // 3
console.log(freq["cat"]);  // 2
console.log(freq["mat"]);  // 1

// Array de-duplication using filter + indexOf
let dupes = [1, 2, 2, 3, 3, 3, 4];
let unique = dupes.filter((val, idx) => dupes.indexOf(val) === idx);
console.log(unique.join(", ")); // 1, 2, 3, 4

// Flatten one level with reduce
let nested = [[1, 2], [3, 4], [5, 6]];
let flat = nested.reduce((acc, arr) => acc.concat(arr), []);
console.log(flat.join(", ")); // 1, 2, 3, 4, 5, 6

// Function returning a function (closure / higher order)
function multiplier(factor) {
    return (n) => n * factor;
}
const triple = multiplier(3);
const double = multiplier(2);
console.log(triple(5));  // 15
console.log(double(7));  // 14

// String reversal
function reverseStr(s) {
    return s.split("").reverse().join("");
}
console.log(reverseStr("javascript")); // tpircasvaj
console.log(reverseStr("racecar"));    // racecar

// Armstrong numbers via array method
function isArmstrong(num) {
    let digits = String(num).split("").map(Number);
    let len = digits.length;
    let sum = digits.reduce((acc, d) => acc + Math.pow(d, len), 0);
    return sum === num;
}
console.log(isArmstrong(153));  // true
console.log(isArmstrong(370));  // true
console.log(isArmstrong(123));  // false

// Max/min from array without Math.max spread
let values = [3, 7, 2, 9, 1, 5];
let maxVal = values.reduce((max, v) => v > max ? v : max, values[0]);
let minVal = values.reduce((min, v) => v < min ? v : min, values[0]);
console.log(maxVal); // 9
console.log(minVal); // 1

// Object array + filter + map chain
let students = [
    { name: "Alice", grade: 88 },
    { name: "Bob",   grade: 55 },
    { name: "Carol", grade: 72 },
    { name: "Dave",  grade: 95 }
];
let passed = students
    .filter(s => s.grade >= 60)
    .map(s => s.name);
console.log(passed.join(", ")); // Alice, Carol, Dave

// Truthy/falsy edge cases
console.log(!!0);          // false
console.log(!!"");         // false
console.log(!!null);       // false
console.log(!!undefined);  // false
console.log(!!NaN);        // false
console.log(!![]);         // false — wait, [] IS truthy
// correction:
console.log(Boolean([]));  // true
console.log(!!{});         // true
console.log(!!1);          // true
console.log(!!"hello");    // true