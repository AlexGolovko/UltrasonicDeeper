"use strict";
let interval;

function updateDepth() {
    var testPoint = 'http://localhost:8080/sonar';
    var prodPoint = 'http://192.168.4.1/';
    interval = setInterval(function () { callSonar() }, 1000);
    function callSonar() {
        $.getJSON(prodPoint, function (data) {
            var item;

            $.each(data, function (key, val) {
                if (key == "depth") {
                    item = "<p id='" + key + "' class='depth'>" + val + "</p>";
                }
            });
            $(".depth").first().before(item);
            var n = $(".depth").length;
            if (n > 10) {
                $(".depth").last().remove()
            }
        });
    }
}

function goToApp() {
    clearInterval(interval);
    console.log('close')
}