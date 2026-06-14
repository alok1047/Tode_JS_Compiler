/*
Expected:
2
4
6
4,8,12
8,12
*/
let arr = [1, 2, 3];

// forEach
arr.forEach(x => console.log(x * 2));

// map
let doubled = arr.map(x => x * 4);
console.log(doubled.join(','));

// filter
let filtered = doubled.filter(x => x > 5);
console.log(filtered.join(','));
