/*
Expected:
empty map: 
empty filter: 
empty reduce: 100
single map: 10
single filter: 5
single reduce: 15
*/
let empty = [];
console.log("empty map: " + empty.map(x => x + 1).join(','));
console.log("empty filter: " + empty.filter(x => x > 0).join(','));
console.log("empty reduce: " + empty.reduce((acc, x) => acc + x, 100));

let single = [5];
console.log("single map: " + single.map(x => x * 2).join(','));
console.log("single filter: " + single.filter(x => x === 5).join(','));
console.log("single reduce: " + single.reduce((acc, x) => acc + x, 10));
