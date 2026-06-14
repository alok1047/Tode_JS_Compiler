/*
Expected:
100
*/
function level1() {
  function level2() {
    function level3() {
      let sum = 0;
      for (let i = 0; i < 5; i++) {
        for (let j = 0; j < 5; j++) {
          sum = sum + 4;
        }
      }
      return sum;
    }
    return level3;
  }
  return level2;
}
console.log(level1()()());
