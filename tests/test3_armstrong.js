function isArmstrong(num) {
    let sum = 0;
    let temp = num;
    let digits = 0;
    
    // Count digits
    let t = num;
    while (t > 0) {
        digits = digits + 1;
        t = Math.floor(t / 10);
    }
    
    t = num;
    while (t > 0) {
        let remainder = t % 10;
        sum = sum + (remainder ** digits);
        t = Math.floor(t / 10);
    }
    
    return sum === num;
}

console.log(isArmstrong(153));
console.log(isArmstrong(154));
