async function sleep(millis) {
    return new Promise(resolve => setTimeout(resolve, millis));
}

async function main() {
    for (let index = 0; index < 10; index++) {
        await sleep(1000);
        console.log(index);
    }
}

main();
