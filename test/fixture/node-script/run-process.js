var cp = require("child_process");

var opt = function () {
/*     exec("notepad.exe", ["arg1", "arg2", "arg3"], function (err, data) {
        console.log(err);
        console.log(data.toString());
    }); */
    cp.exec("notepad.exe", ["arg1", "arg2", "arg3"]);
};
opt();
