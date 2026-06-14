// === Hidden TC Feature Tests ===

// 1. Arrow functions + map/filter/reduce
let numbers = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
let evens = numbers.filter((n) => n % 2 === 0);
console.log("Evens: " + evens.join(", "));

let doubled = numbers.map((n) => n * 2);
console.log("Doubled: " + doubled.join(", "));

let sum = numbers.reduce((acc, n) => acc + n, 0);
console.log("Sum: " + sum);

// 2. find, some, every
let found = numbers.find((n) => n > 7);
console.log("First > 7: " + found);

let hasNeg = numbers.some((n) => n < 0);
console.log("Has negative: " + hasNeg);

let allPos = numbers.every((n) => n > 0);
console.log("All positive: " + allPos);

// 3. const
const PI = 3.14159;
console.log("PI: " + PI);

// 4. String methods
let greeting = "  Hello, World!  ";
console.log(greeting.trim());
console.log(greeting.trim().toUpperCase());
console.log(greeting.trim().toLowerCase());
console.log("hello".startsWith("hel"));
console.log("hello".endsWith("llo"));
console.log("hello".includes("ell"));
console.log("hello world".indexOf("world"));
console.log("hello world".replace("world", "JS"));
console.log("a-b-c".split("-").join(", "));

// 5. Switch statement
let day = 3;
switch (day) {
    case 1:
        console.log("Monday");
        break;
    case 2:
        console.log("Tuesday");
        break;
    case 3:
        console.log("Wednesday");
        break;
    default:
        console.log("Other day");
}

// 6. do-while
let count = 0;
do {
    count++;
} while (count < 5);
console.log("Count: " + count);

// 7. Array push/pop/shift/unshift
let stack = [1, 2, 3];
stack.push(4);
console.log("After push: " + stack.join(", "));
let popped = stack.pop();
console.log("Popped: " + popped);
console.log("After pop: " + stack.join(", "));
stack.unshift(0);
console.log("After unshift: " + stack.join(", "));
let shifted = stack.shift();
console.log("Shifted: " + shifted);
console.log("After shift: " + stack.join(", "));

// 8. Array slice/splice/concat/includes/indexOf
let arr = [10, 20, 30, 40, 50];
console.log("Slice(1,3): " + arr.slice(1, 3).join(", "));
console.log("Includes 30: " + arr.includes(30));
console.log("IndexOf 40: " + arr.indexOf(40));
let arr2 = arr.concat([60, 70]);
console.log("Concat: " + arr2.join(", "));

// 9. Spread operator with arrays
let a = [1, 2, 3];
let b = [4, 5, 6];
let merged = [...a, ...b];
console.log("Spread merge: " + merged.join(", "));

// 10. Rest parameters
function sumAll(...nums) {
    return nums.reduce((a, b) => a + b, 0);
}
console.log("Rest sum: " + sumAll(1, 2, 3, 4, 5));

// 11. Math object
console.log("Floor: " + Math.floor(4.7));
console.log("Ceil: " + Math.ceil(4.2));
console.log("Round: " + Math.round(4.5));
console.log("Abs: " + Math.abs(-5));
console.log("Max: " + Math.max(1, 5, 3));
console.log("Min: " + Math.min(1, 5, 3));
console.log("Pow: " + Math.pow(2, 3));
console.log("Sqrt: " + Math.sqrt(16));

// 12. Ternary operator
let age = 20;
let status = age >= 18 ? "adult" : "minor";
console.log("Status: " + status);

// 13. Type coercion
console.log("5" + 3);
console.log("5" - 3);

// 14. Substring
console.log("Hello World".substring(0, 5));

// 15. Object manipulation
let person = { name: "Alice", age: 25 };
console.log(person.name);
console.log(person.age);
person.city = "NYC";
console.log(person.city);
