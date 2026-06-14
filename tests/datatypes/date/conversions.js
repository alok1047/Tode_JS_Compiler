/*
Expected:
1970-01-01T00:00:00.000Z
Thu, 01 Jan 1970 00:00:00 GMT
1970-01-01T00:00:00.000Z
*/
let d = new Date(0);
console.log(d.toISOString());
console.log(d.toUTCString());
console.log(d.toJSON());
