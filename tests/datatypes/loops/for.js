/*
Expected:
0
1
2
0
1
2
4
0,0
0,1
1,0
1,1
*/
// break test
for (let i = 0; i < 5; i++) {
  if (i === 3) break;
  console.log(i);
}

// continue test
for (let i = 0; i < 5; i++) {
  if (i === 3) continue;
  console.log(i);
}

// nested loop
for (let i = 0; i < 2; i++) {
  for (let j = 0; j < 2; j++) {
    console.log(i + "," + j);
  }
}
