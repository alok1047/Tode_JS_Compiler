// Date Subsystem Test Suite for ThunderJS

let passed = 0;
let failed = 0;

function assert(condition, message) {
    if (condition) {
        passed = passed + 1;
        // Optional verbose log:
        // console.log("PASS: " + message);
    } else {
        failed = failed + 1;
        console.log("FAIL: " + message);
    }
}

console.log("⚡ Running Date Subsystem Test Suite...");
console.log("======================================");

// Test 1: typeof of Date instance
assert(typeof new Date() === "object", "typeof Date instance should be 'object'");

// Test 2: typeof of Date constructor
assert(typeof Date === "function", "typeof Date constructor should be 'function'");

// Test 3: empty constructor
let nowObj = new Date();
assert(nowObj.getTime() > 1700000000000, "new Date() should represent current time");

// Test 4: constructor with 0
let epoch = new Date(0);
assert(epoch.getTime() === 0, "new Date(0) should represent epoch 0");

// Test 5: constructor with 1000
let sec1 = new Date(1000);
assert(sec1.getTime() === 1000, "new Date(1000) should represent epoch 1000");

// Test 6: constructor with date string
let strDate = new Date("2025-06-13");
assert(strDate.getFullYear() === 2025, "new Date('2025-06-13').getFullYear() should be 2025");
assert(strDate.getMonth() === 5, "new Date('2025-06-13').getMonth() should be 5 (June)");
assert(strDate.getDate() === 13, "new Date('2025-06-13').getDate() should be 13");

// Test 7: constructor with 3 arguments (year, month, day)
let date3 = new Date(2025, 5, 13);
assert(date3.getFullYear() === 2025, "new Date(2025,5,13) year should be 2025");
assert(date3.getMonth() === 5, "new Date(2025,5,13) month should be 5");
assert(date3.getDate() === 13, "new Date(2025,5,13) date should be 13");

// Test 8: constructor with 6 arguments (year, month, day, hours, minutes, seconds)
let date6 = new Date(2025, 5, 13, 10, 30, 45);
assert(date6.getHours() === 10, "new Date(...) hours should be 10");
assert(date6.getMinutes() === 30, "new Date(...) minutes should be 30");
assert(date6.getSeconds() === 45, "new Date(...) seconds should be 45");

// Test 9: constructor with 7 arguments (including milliseconds)
let date7 = new Date(2025, 5, 13, 10, 30, 45, 123);
assert(date7.getMilliseconds() === 123, "new Date(...) milliseconds should be 123");

// Test 10: getDay method
let dayTest = new Date(2025, 5, 13); // June 13, 2025 is Friday (5)
assert(dayTest.getDay() === 5, "June 13, 2025 day of week should be 5 (Friday)");

// Test 11: Date.now() static method
let nowMs = Date.now();
assert(nowMs > 1700000000000, "Date.now() should return a valid current timestamp");

// Test 12: toISOString
let isoStr = new Date(0).toISOString();
assert(isoStr === "1970-01-01T00:00:00.000Z", "new Date(0).toISOString() should be ISO epoch string");

// Test 13: toUTCString
let utcStr = new Date(0).toUTCString();
assert(utcStr === "Thu, 01 Jan 1970 00:00:00 GMT", "new Date(0).toUTCString() should be GMT epoch string");

// Test 14: toJSON
let jsonStr = new Date(0).toJSON();
assert(jsonStr === "1970-01-01T00:00:00.000Z", "new Date(0).toJSON() should match toISOString");

// Test 15: Date.parse()
let parsedEpoch = Date.parse("1970-01-01T00:00:00.000Z");
assert(parsedEpoch === 0, "Date.parse('1970-01-01T00:00:00.000Z') should be 0");

// Test 16: Date.UTC()
let utcEpoch = Date.UTC(1970, 0, 1, 0, 0, 0, 0);
assert(utcEpoch === 0, "Date.UTC(1970, 0, 1) should be 0");

// Test 17: Auto-rolling month in constructor
let rolledMonth = new Date(2025, 12, 1); // Month index 12 -> Jan 2026
assert(rolledMonth.getFullYear() === 2026, "month 12 should roll to year 2026");
assert(rolledMonth.getMonth() === 0, "month 12 should roll to month 0 (January)");

// Test 18: Auto-rolling day in constructor
let rolledDay = new Date(2025, 0, 32); // Jan 32 -> Feb 1
assert(rolledDay.getFullYear() === 2025, "day 32 should keep year 2025");
assert(rolledDay.getMonth() === 1, "day 32 should roll to month 1 (February)");
assert(rolledDay.getDate() === 1, "day 32 should roll to day 1");

// Test 19: setFullYear mutation
let mutDate = new Date(0);
mutDate.setFullYear(2030);
assert(mutDate.getFullYear() === 2030, "setFullYear should mutate year");

// Test 20: setMonth mutation
let mutMonthObj = new Date(0);
mutMonthObj.setMonth(5);
assert(mutMonthObj.getMonth() === 5, "setMonth should mutate month");

// Test 21: setDate mutation
let mutDayObj = new Date(0);
mutDayObj.setDate(25);
assert(mutDayObj.getDate() === 25, "setDate should mutate day");

// Test 22: setHours mutation
let mutHoursObj = new Date(0);
mutHoursObj.setHours(15);
assert(mutHoursObj.getHours() === 15, "setHours should mutate hours");

// Test 23: setMinutes mutation
let mutMinsObj = new Date(0);
mutMinsObj.setMinutes(45);
assert(mutMinsObj.getMinutes() === 45, "setMinutes should mutate minutes");

// Test 24: setSeconds mutation
let mutSecsObj = new Date(0);
mutSecsObj.setSeconds(30);
assert(mutSecsObj.getSeconds() === 30, "setSeconds should mutate seconds");

// Test 25: setMilliseconds mutation
let mutMsObj = new Date(0);
mutMsObj.setMilliseconds(500);
assert(mutMsObj.getMilliseconds() === 500, "setMilliseconds should mutate milliseconds");

// Test 26: setTime mutation
let mutTimeObj = new Date(0);
mutTimeObj.setTime(20000);
assert(mutTimeObj.getTime() === 20000, "setTime should mutate timestamp");

// Test 27: Invalid Date creation
let invalidDate = new Date("invalid date string");
assert(invalidDate.toString() === "Invalid Date", "invalid input should return 'Invalid Date' string");

// Test 28: Invalid Date getters
assert(typeof invalidDate.getFullYear() === "number" && invalidDate.getFullYear() !== invalidDate.getFullYear(), "invalid date getFullYear() should be NaN");

// Test 29: Invalid Date toJSON
assert(invalidDate.toJSON() === null, "invalid date toJSON() should return null");

// Test 30: Timezone offset check
let tzOffset = new Date().getTimezoneOffset();
assert(typeof tzOffset === "number" && !isNaN(tzOffset), "getTimezoneOffset() should return a valid number");

// Test 31: Relational comparison (> and <)
let early = new Date(500);
let late = new Date(1000);
assert(late > early, "Date relational operation > should work");
assert(early < late, "Date relational operation < should work");

// Test 32: String method toDateString format
let dateStrOnly = new Date(2025, 5, 13).toDateString();
assert(dateStrOnly.startsWith("Fri Jun 13 2025") || dateStrOnly.startsWith("Sat Jun 14 2025"), "toDateString format test");


if (failed === 0) {
    console.log("ALL DATE TESTS PASSED! 🎉");
} else {
    console.log("SOME DATE TESTS FAILED! ❌");
}
