"use strict";
var interval;
var testPoint = 'http://localhost:8080/sonar';
var prodPoint = 'http://192.168.4.1/';

function updateDepth(endpoind) {
    interval = setInterval(function () { callSonar() }, 1000);
    function callSonar() {
        $.getJSON(endpoind, function (data) {
            $(".label").css("color", "white");
            var item;
            $.each(data, function (key, val) {
                if (key == "depth") {
                    item = "<p  class='depth'>" + parseFloat(val).toFixed(2) + "</p>";
                }
            });
            $(".depth").first().before(item);

            if ($(".depth").length > 6) {
                $(".depth").last().remove()
            }
        }).catch(function () {
            $(".label").css("color", "red");
        });
    }
}

function callEndpoint() {
    $(".label").css("color", "red");
    Promise.resolve(
        $.ajax({
            url: prodPoint,
            timeout: 1000,//1 second timeout
        })
    ).then(function () {
        updateDepth(prodPoint)
    }).catch(function (e) {
        updateDepth(testPoint)
    });
}

function goToApp() {
    clearInterval(interval);
    console.log('close')
}