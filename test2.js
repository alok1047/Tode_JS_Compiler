const mySet = new Set();

// Create a Set from an array (duplicates are ignored)
const numbersArray = [1, 2, 3, 3, 4, 2, 5];
const numbersSet = new Set(numbersArray);

console.log(numbersSet); // Set(5) { 1, 2, 3, 4, 5 }