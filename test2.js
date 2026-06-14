console.log("=== HIDDEN TEST KILLER ===");

/* ---------------- CLOSURE ---------------- */

function outer() {
    let x = 10;

    function inner() {
        return x;
    }

    return inner;
}

const fn = outer();

console.log(fn()); // 10

/* ---------------- CLOSURE MUTATION ---------------- */

function counter() {
    let count = 0;

    return function () {
        count++;
        return count;
    };
}

const c = counter();

console.log(c());
console.log(c());
console.log(c());

/* ---------------- THIS ---------------- */

const obj = {
    value: 10,

    getValue() {
        return this.value;
    }
};

console.log(obj.getValue());

/* ---------------- NESTED PROPERTY ASSIGNMENT ---------------- */

const user = {
    stats: {
        score: 5
    }
};

user.stats.score = 20;

console.log(user.stats.score);

/* ---------------- DEEP PROPERTY ACCESS ---------------- */

const deep = {
    a: {
        b: {
            c: 42
        }
    }
};

console.log(deep.a.b.c);

/* ---------------- COMPOUND ASSIGNMENT ---------------- */

const scoreObj = {
    score: 10
};

scoreObj.score += 5;

console.log(scoreObj.score);

/* ---------------- PREFIX / POSTFIX ---------------- */

const hero = {
    level: 10
};

hero.level++;
++hero.level;

console.log(hero.level);

/* ---------------- ARRAY COMPOUND ASSIGNMENT ---------------- */

const arr = [1, 2, 3];

arr[1] += 10;

console.log(arr[1]);

/* ---------------- SHORT CIRCUIT ---------------- */

console.log(false && 123);
console.log(true || 456);

let x = 0;

true || (x = 100);

console.log(x);

/* ---------------- TRUTHY FALSY ---------------- */

if ("")
    console.log("BAD");
else
    console.log("EMPTY_STRING_OK");

if (0)
    console.log("BAD");
else
    console.log("ZERO_OK");

if ("hello")
    console.log("STRING_OK");

/* ---------------- FUNCTION HOISTING ---------------- */

hoisted();

function hoisted() {
    console.log("HOISTED_OK");
}

/* ---------------- FUNCTION EXPRESSION ---------------- */

const bar = function () {
    return "BAR_OK";
};

console.log(bar());

/* ---------------- RECURSION ---------------- */

function fact(n) {
    if (n <= 1) return 1;

    return n * fact(n - 1);
}

console.log(fact(8));

/* ---------------- OBJECT REFERENCES ---------------- */

const objA = {
    value: 10
};

const objB = objA;

objB.value = 99;

console.log(objA.value);

/* ---------------- ARRAY REFERENCES ---------------- */

const arrA = [1, 2, 3];
const arrB = arrA;

arrB.push(4);

console.log(arrA.join(","));

/* ---------------- CALLBACK RETURNING OBJECT ---------------- */

const mapped = [1, 2, 3].map(x => ({
    value: x
}));

console.log(mapped[0].value);
console.log(mapped[2].value);

/* ---------------- NESTED DESTRUCTURING DEFAULT ---------------- */

const profileUser = {
    profile: {}
};

const {
    profile: {
        role = "user"
    }
} = profileUser;

console.log(role);

/* ---------------- DYNAMIC PROPERTY CREATION ---------------- */

const key = "name";

const dynamicObj = {};

dynamicObj[key] = "Alok";

console.log(dynamicObj.name);

/* ---------------- ARRAY LENGTH ---------------- */

const nums = [];

nums.push(1);
nums.push(2);
nums.pop();

console.log(nums.length);

/* ---------------- FOR OF ARRAY ---------------- */

for (const num of [10, 20, 30]) {
    console.log("FOROF:" + num);
}

/* ---------------- FOR IN OBJECT ---------------- */

const car = {
    brand: "Honda",
    year: 2025
};

for (const k in car) {
    console.log("FORIN:" + k);
}

/* ---------------- OBJECT ENTRIES LOOP ---------------- */

for (const [k, v] of Object.entries(car)) {
    console.log(k + "=" + v);
}

/* ---------------- SPARSE ARRAY ---------------- */

const sparse = [];

sparse[100] = 5;

console.log(sparse.length);
console.log(sparse[100]);
console.log(sparse[50]);

/* ---------------- ARRAY HOLE ---------------- */

const hole = [1,,3];

console.log(hole.length);
console.log(hole[1]);

/* ---------------- SET ---------------- */

const set = new Set();

set.add(1).add(2).add(3);

console.log(set.size);

for (const v of set) {
    console.log("SET:" + v);
}

/* ---------------- MAP ---------------- */

const map = new Map();

const person = {
    id: 1
};

map.set(person, "admin");

console.log(map.get(person));

/* ---------------- DATE ---------------- */

const d = new Date(2025, 5, 14);

console.log(typeof Date.now());

console.log(d.getFullYear());
console.log(d.getMonth());
console.log(d.getDate());

d.setFullYear(2030);

console.log(d.getFullYear());

/* ---------------- ARRAY METHODS ---------------- */

console.log([1,2,3].includes(2));
console.log([1,2,3].indexOf(3));
console.log([1,2,3,2].lastIndexOf(2));

console.log(
    [1,2,3].join("-")
);

console.log(
    [1,[2,3],[4]].flat().join(",")
);

console.log(
    [1,2,3,4].slice(1,3).join(",")
);

const sp = [1,2,3,4];
sp.splice(1,2);

console.log(sp.join(","));

/* ---------------- SHIFT UNSHIFT ---------------- */

const q = [2,3];

q.unshift(1);

console.log(q.join(","));

q.shift();

console.log(q.join(","));

/* ---------------- OBJECT KEYS VALUES ---------------- */

console.log(
    Object.keys({a:1,b:2}).join(",")
);

console.log(
    Object.values({a:1,b:2}).join(",")
);

console.log("=== ALL TESTS PASSED ===");