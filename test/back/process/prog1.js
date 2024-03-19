const args = process.argv; 

console.log("hello world");
args.forEach((arg, idx) => console.log(`${idx} arg = ${arg}`));

console.log("ENV - MY_VAR = ", process.env.MY_VAR)

