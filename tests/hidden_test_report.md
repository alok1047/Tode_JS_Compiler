# Hidden-Test Stress Suite Report — ThunderJS

This report details the execution results of a 50-program stress suite designed to validate execution correctness and find edge cases in the ThunderJS interpreter.

## 1. Arithmetic precedence
* **Source Code:**
```javascript
console.log(2 + 3 * 4 ** 2 - 10 / 2);
```
* **Expected Output:**
```
45

```
* **Actual Output:**
```
45

```
* **Result:** PASSED ✅

---

## 2. Variable shadowing
* **Source Code:**
```javascript
let x = 10; { let x = 20; console.log(x); } console.log(x);
```
* **Expected Output:**
```
20
10

```
* **Actual Output:**
```
20
10

```
* **Result:** PASSED ✅

---

## 3. Constant reassignment error
* **Source Code:**
```javascript
const x = 10; x = 20;
```
* **Expected Output:**
```
TypeError: Assignment to constant variable 'x'
```
* **Actual Output:**
```
TypeError: Assignment to constant variable 'x'
```
* **Result:** PASSED ✅

---

## 4. Missing variable reference error
* **Source Code:**
```javascript
console.log(y);
```
* **Expected Output:**
```
ReferenceError: y is not defined
```
* **Actual Output:**
```
ReferenceError: y is not defined
```
* **Result:** PASSED ✅

---

## 5. Function parameter shadowing
* **Source Code:**
```javascript
let x = 5; function f(x) { console.log(x); } f(10); console.log(x);
```
* **Expected Output:**
```
10
5

```
* **Actual Output:**
```
10
5

```
* **Result:** PASSED ✅

---

## 6. Closure variable capture
* **Source Code:**
```javascript
function f() { let x = 10; return () => { x += 5; return x; }; } let g = f(); console.log(g()); console.log(g());
```
* **Expected Output:**
```
15
20

```
* **Actual Output:**
```
15
20

```
* **Result:** PASSED ✅

---

## 7. Recursion (factorial)
* **Source Code:**
```javascript
function fact(n) { if (n <= 1) return 1; return n * fact(n - 1); } console.log(fact(5));
```
* **Expected Output:**
```
120

```
* **Actual Output:**
```
120

```
* **Result:** PASSED ✅

---

## 8. Recursion (Fibonacci)
* **Source Code:**
```javascript
function fib(n) { if (n <= 1) return n; return fib(n - 1) + fib(n - 2); } console.log(fib(7));
```
* **Expected Output:**
```
13

```
* **Actual Output:**
```
13

```
* **Result:** PASSED ✅

---

## 9. Array push/pop/shift/unshift
* **Source Code:**
```javascript
let a = [2]; a.push(3); a.unshift(1); console.log(a.join(',')); console.log(a.pop()); console.log(a.shift()); console.log(a.join(','));
```
* **Expected Output:**
```
1,2,3
3
1
2

```
* **Actual Output:**
```
1,2,3
3
1
2

```
* **Result:** PASSED ✅

---

## 10. Array reverse/join
* **Source Code:**
```javascript
let a = [1, 2, 3]; a.reverse(); console.log(a.join('-'));
```
* **Expected Output:**
```
3-2-1

```
* **Actual Output:**
```
3-2-1

```
* **Result:** PASSED ✅

---

## 11. Array includes/indexOf/lastIndexOf
* **Source Code:**
```javascript
let a = [1, 2, 3, 2]; console.log(a.includes(2)); console.log(a.indexOf(2)); console.log(a.lastIndexOf(2));
```
* **Expected Output:**
```
true
1
3

```
* **Actual Output:**
```
true
1
3

```
* **Result:** PASSED ✅

---

## 12. Array slice/splice/concat
* **Source Code:**
```javascript
let a = [1, 2, 3, 4]; console.log(a.slice(1, 3).join(',')); a.splice(1, 2, 8, 9); console.log(a.join(',')); console.log(a.concat([5, 6]).join(','));
```
* **Expected Output:**
```
2,3
1,8,9,4
1,8,9,4,5,6

```
* **Actual Output:**
```
2,3
1,8,9,4
1,8,9,4,5,6

```
* **Result:** PASSED ✅

---

## 13. Array sort
* **Source Code:**
```javascript
let a = [10, 2, 30, 4]; a.sort((x, y) => x - y); console.log(a.join(','));
```
* **Expected Output:**
```
2,4,10,30

```
* **Actual Output:**
```
2,4,10,30

```
* **Result:** PASSED ✅

---

## 14. Higher-order array map
* **Source Code:**
```javascript
let a = [1, 2, 3]; console.log(a.map(x => x * 3).join(','));
```
* **Expected Output:**
```
3,6,9

```
* **Actual Output:**
```
3,6,9

```
* **Result:** PASSED ✅

---

## 15. Higher-order array filter
* **Source Code:**
```javascript
let a = [1, 2, 3, 4]; console.log(a.filter(x => x % 2 !== 0).join(','));
```
* **Expected Output:**
```
1,3

```
* **Actual Output:**
```
1,3

```
* **Result:** PASSED ✅

---

## 16. Higher-order array reduce
* **Source Code:**
```javascript
let a = [1, 2, 3, 4]; console.log(a.reduce((sum, x) => sum + x, 10));
```
* **Expected Output:**
```
20

```
* **Actual Output:**
```
20

```
* **Result:** PASSED ✅

---

## 17. Higher-order array find/findIndex
* **Source Code:**
```javascript
let a = [10, 20, 30]; console.log(a.find(x => x > 15)); console.log(a.findIndex(x => x > 15));
```
* **Expected Output:**
```
20
1

```
* **Actual Output:**
```
20
1

```
* **Result:** PASSED ✅

---

## 18. Higher-order array some/every/forEach
* **Source Code:**
```javascript
let a = [1, 2, 3]; console.log(a.some(x => x > 2)); console.log(a.every(x => x > 0)); let sum = 0; a.forEach(x => { sum += x; }); console.log(sum);
```
* **Expected Output:**
```
true
true
6

```
* **Actual Output:**
```
true
true
6

```
* **Result:** PASSED ✅

---

## 19. Array flat/fill
* **Source Code:**
```javascript
let a = [1, [2, 3]]; console.log(a.flat().join(',')); let b = [1, 2, 3]; b.fill(9, 1, 3); console.log(b.join(','));
```
* **Expected Output:**
```
1,2,3
1,9,9

```
* **Actual Output:**
```
1,2,3
1,9,9

```
* **Result:** PASSED ✅

---

## 20. String length and case methods
* **Source Code:**
```javascript
let s = 'Hello'; console.log(s.length); console.log(s.toUpperCase()); console.log(s.toLowerCase());
```
* **Expected Output:**
```
5
HELLO
hello

```
* **Actual Output:**
```
5
HELLO
hello

```
* **Result:** PASSED ✅

---

## 21. String trim/trimStart/trimEnd
* **Source Code:**
```javascript
let s = ' hello '; console.log(s.trim()); console.log(s.trimStart()); console.log(s.trimEnd());
```
* **Expected Output:**
```
hello
hello 
 hello

```
* **Actual Output:**
```
hello
hello 
 hello

```
* **Result:** PASSED ✅

---

## 22. String split/join combination
* **Source Code:**
```javascript
let s = 'a,b,c'; console.log(s.split(',').join('|'));
```
* **Expected Output:**
```
a|b|c

```
* **Actual Output:**
```
a|b|c

```
* **Result:** PASSED ✅

---

## 23. String replace/replaceAll
* **Source Code:**
```javascript
let s = 'hello hello'; console.log(s.replace('hello', 'hi')); console.log(s.replaceAll('hello', 'hi'));
```
* **Expected Output:**
```
hi hello
hi hi

```
* **Actual Output:**
```
hi hello
hi hi

```
* **Result:** PASSED ✅

---

## 24. String substring/slice
* **Source Code:**
```javascript
let s = 'javascript'; console.log(s.substring(4, 10)); console.log(s.slice(-6));
```
* **Expected Output:**
```
script
script

```
* **Actual Output:**
```
script
script

```
* **Result:** PASSED ✅

---

## 25. String indexOf/includes/startsWith/endsWith
* **Source Code:**
```javascript
let s = 'hello world'; console.log(s.indexOf('o')); console.log(s.includes('world')); console.log(s.startsWith('he')); console.log(s.endsWith('ld'));
```
* **Expected Output:**
```
4
true
true
true

```
* **Actual Output:**
```
4
true
true
true

```
* **Result:** PASSED ✅

---

## 26. String charAt/charCodeAt/repeat
* **Source Code:**
```javascript
let s = 'abc'; console.log(s.charAt(1)); console.log(s.charCodeAt(0)); console.log(s.repeat(3));
```
* **Expected Output:**
```
b
97
abcabcabc

```
* **Actual Output:**
```
b
97
abcabcabc

```
* **Result:** PASSED ✅

---

## 27. String padStart/padEnd/concat
* **Source Code:**
```javascript
let s = '5'; console.log(s.padStart(3, '0')); console.log(s.padEnd(3, '0')); console.log(s.concat('a', 'b'));
```
* **Expected Output:**
```
005
500
5ab

```
* **Actual Output:**
```
005
500
5ab

```
* **Result:** PASSED ✅

---

## 28. Object literal simple access
* **Source Code:**
```javascript
let o = { x: 1, y: 2 }; console.log(o.x); console.log(o.y);
```
* **Expected Output:**
```
1
2

```
* **Actual Output:**
```
1
2

```
* **Result:** PASSED ✅

---

## 29. Object property assignment
* **Source Code:**
```javascript
let o = {}; o.x = 42; console.log(o.x);
```
* **Expected Output:**
```
42

```
* **Actual Output:**
```
42

```
* **Result:** PASSED ✅

---

## 30. Object computed property access
* **Source Code:**
```javascript
let o = { a: 10 }; let k = 'a'; console.log(o[k]); o[k] = 20; console.log(o.a);
```
* **Expected Output:**
```
10
20

```
* **Actual Output:**
```
10
20

```
* **Result:** PASSED ✅

---

## 31. Object.keys/values/entries
* **Source Code:**
```javascript
let o = { a: 1, b: 2 }; console.log(Object.keys(o).join(',')); console.log(Object.values(o).join(','));
```
* **Expected Output:**
```
a,b
1,2

```
* **Actual Output:**
```
a,b
1,2

```
* **Result:** PASSED ✅

---

## 32. Switch case matching
* **Source Code:**
```javascript
let x = 'a'; switch(x) { case 'a': console.log(1); break; case 'b': console.log(2); break; }
```
* **Expected Output:**
```
1

```
* **Actual Output:**
```
1

```
* **Result:** PASSED ✅

---

## 33. Switch case fallthrough
* **Source Code:**
```javascript
let x = 'a'; switch(x) { case 'a': console.log(1); case 'b': console.log(2); break; }
```
* **Expected Output:**
```
1
2

```
* **Actual Output:**
```
1
2

```
* **Result:** PASSED ✅

---

## 34. Switch case default fallback
* **Source Code:**
```javascript
let x = 'z'; switch(x) { case 'a': console.log(1); break; default: console.log(3); }
```
* **Expected Output:**
```
3

```
* **Actual Output:**
```
3

```
* **Result:** PASSED ✅

---

## 35. Do-while loop execution
* **Source Code:**
```javascript
let i = 0; do { console.log(i); i++; } while (i < 2);
```
* **Expected Output:**
```
0
1

```
* **Actual Output:**
```
0
1

```
* **Result:** PASSED ✅

---

## 36. Break in for loop
* **Source Code:**
```javascript
for (let i = 0; i < 5; i++) { if (i === 3) break; console.log(i); }
```
* **Expected Output:**
```
0
1
2

```
* **Actual Output:**
```
0
1
2

```
* **Result:** PASSED ✅

---

## 37. Continue in for loop
* **Source Code:**
```javascript
for (let i = 0; i < 5; i++) { if (i === 3) continue; console.log(i); }
```
* **Expected Output:**
```
0
1
2
4

```
* **Actual Output:**
```
0
1
2
4

```
* **Result:** PASSED ✅

---

## 38. Nested loops (for inside while)
* **Source Code:**
```javascript
let i = 0; while (i < 2) { for (let j = 0; j < 2; j++) { console.log(i + ',' + j); } i++; }
```
* **Expected Output:**
```
0,0
0,1
1,0
1,1

```
* **Actual Output:**
```
0,0
0,1
1,0
1,1

```
* **Result:** PASSED ✅

---

## 39. Ternary operator true branch
* **Source Code:**
```javascript
console.log(true ? 'yes' : 'no');
```
* **Expected Output:**
```
yes

```
* **Actual Output:**
```
yes

```
* **Result:** PASSED ✅

---

## 40. Ternary operator false branch
* **Source Code:**
```javascript
console.log(false ? 'yes' : 'no');
```
* **Expected Output:**
```
no

```
* **Actual Output:**
```
no

```
* **Result:** PASSED ✅

---

## 41. Loose vs strict equality
* **Source Code:**
```javascript
console.log('5' == 5); console.log('5' === 5);
```
* **Expected Output:**
```
true
false

```
* **Actual Output:**
```
true
false

```
* **Result:** PASSED ✅

---

## 42. Loose vs strict inequality
* **Source Code:**
```javascript
console.log('5' != 5); console.log('5' !== 5);
```
* **Expected Output:**
```
false
true

```
* **Actual Output:**
```
false
true

```
* **Result:** PASSED ✅

---

## 43. Loose equality for null/undefined
* **Source Code:**
```javascript
console.log(null == undefined); console.log(null === undefined);
```
* **Expected Output:**
```
true
false

```
* **Actual Output:**
```
true
false

```
* **Result:** PASSED ✅

---

## 44. Loose equality for truthy/falsy coercion
* **Source Code:**
```javascript
console.log(0 == ''); console.log(0 == false);
```
* **Expected Output:**
```
true
true

```
* **Actual Output:**
```
true
true

```
* **Result:** PASSED ✅

---

## 45. Prefix/postfix increment/decrement
* **Source Code:**
```javascript
let x = 5; console.log(x++); console.log(x); console.log(++x); console.log(x--); console.log(x); console.log(--x);
```
* **Expected Output:**
```
5
6
7
7
6
5

```
* **Actual Output:**
```
5
6
7
7
6
5

```
* **Result:** PASSED ✅

---

## 46. Compound assignment
* **Source Code:**
```javascript
let x = 10; x += 5; console.log(x); x -= 3; console.log(x); x *= 2; console.log(x); x /= 4; console.log(x);
```
* **Expected Output:**
```
15
12
24
6

```
* **Actual Output:**
```
15
12
24
6

```
* **Result:** PASSED ✅

---

## 47. Logical short-circuiting
* **Source Code:**
```javascript
let x = 0; console.log(true || (x = 5)); console.log(x); console.log(false && (x = 10)); console.log(x);
```
* **Expected Output:**
```
true
0
false
0

```
* **Actual Output:**
```
true
0
false
0

```
* **Result:** PASSED ✅

---

## 48. Typeof operator checks
* **Source Code:**
```javascript
console.log(typeof 42); console.log(typeof 'hi'); console.log(typeof true); console.log(typeof undefined); console.log(typeof {});
```
* **Expected Output:**
```
number
string
boolean
undefined
object

```
* **Actual Output:**
```
number
string
boolean
undefined
object

```
* **Result:** PASSED ✅

---

## 49. parseInt and parseFloat
* **Source Code:**
```javascript
console.log(parseInt('101', 2)); console.log(parseFloat('3.14'));
```
* **Expected Output:**
```
5
3.14

```
* **Actual Output:**
```
5
3.14

```
* **Result:** PASSED ✅

---

## 50. Template literals with interpolation
* **Source Code:**
```javascript
let name = 'Grace'; let age = 30; console.log(`Hello, ${name}! Next year you will be ${age + 1}.`);
```
* **Expected Output:**
```
Hello, Grace! Next year you will be 31.

```
* **Actual Output:**
```
Hello, Grace! Next year you will be 31.

```
* **Result:** PASSED ✅

---

## Final Summary
* **Total Tests:** 50
* **Passed:** 50
* **Failed:** 0
* **Success Rate:** 100.0%
