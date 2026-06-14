// Template literals
let name = "World";
console.log(`Hello, ${name}!`);
console.log(`2 + 3 = ${2 + 3}`);

// Function expression
let square = function(x) {
    return x * x;
};
console.log("Square of 5: " + square(5));

// Callback function
function operate(a, b, callback) {
    return callback(a, b);
}
let result = operate(10, 5, function(x, y) { return x + y; });
console.log("Callback result: " + result);

let result2 = operate(10, 5, (x, y) => x - y);
console.log("Arrow callback: " + result2);

// Array sort
let nums = [3, 1, 4, 1, 5, 9, 2, 6];
let sorted = [...nums].sort((a, b) => a - b);
console.log("Sorted: " + sorted.join(", "));

// sort reverse
let sortedDesc = [...nums].sort((a, b) => b - a);
console.log("Sorted desc: " + sortedDesc.join(", "));

// replaceAll
let text = "foo bar foo baz foo";
console.log(text.replaceAll("foo", "qux"));

// substring vs slice on strings
let s = "Hello World";
console.log(s.substring(6));
console.log(s.slice(-5));

// ++/-- operators
let x = 5;
x++;
console.log("After x++: " + x);
x--;
console.log("After x--: " + x);
console.log("++x: " + (++x));
console.log("--x: " + (--x));

// Null and undefined
let u;
console.log(u);
let n = null;
console.log(n);

// Boolean
console.log(true);
console.log(false);

// Nested function
function outer() {
    let x = 10;
    function inner() {
        return x + 5;
    }
    return inner();
}
console.log("Nested: " + outer());

// Default parameters
function greet(name, greeting) {
    if (greeting === undefined) {
        greeting = "Hello";
    }
    return greeting + ", " + name + "!";
}
console.log(greet("Alice"));
console.log(greet("Bob", "Hi"));

// Splice
let arr = [1, 2, 3, 4, 5];
arr.splice(2, 1);
console.log("After splice: " + arr.join(", "));
