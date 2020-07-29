"use strict";
var interval;
var testPoint = 'http://localhost:8080/sonar';
var prodPoint = 'http://192.168.4.1/';

function updateDepth(endpoind) {
    interval = setInterval(function () { callSonar() }, 500);
    function callSonar() {
        $.getJSON(endpoind, function (data) {
            $(".label").css("color", "white");
            var item;
            $.each(data, function (key, val) {
                if (key == "depth") {
                    item = "<p  class='depth'>" + parseFloat(val).toFixed(2) + "</p>";
                }
                if (key == "battery") {
                    updateBatteryLevel(val);
                }
                if (key == "temperature") {
                    updateTemperature(val);
                }
            });
            $(".depth").first().before(item);

            if ($(".depth").length > 5) {
                $(".depth").last().remove()
            }
        }).catch(function () {
            $(".label").css("color", "red");
        });
    }
}

function updateTemperature(temp) {
    $(".temperature").text(parseFloat(temp).toFixed(1) + ' °C');
}

function updateBatteryLevel(battery) {
    var battery_vcc = 4.3 * (parseFloat(battery).toFixed(2) / 1023) - 0.1;
    console.log(battery_vcc);
    var charge = 100;
    for (let [key, value] of levels.entries()) {
        if (battery_vcc > key) {
            charge = value;
            break;
        }
    }
    $(".battery_level").text(charge + '%');
}
let levels = new Map([
    [4.2, 100],
    [4.1, 90],
    [4.0, 80],
    [3.9, 70],
    [3.8, 60],
    [3.7, 50],
    [3.6, 40],
    [3.5, 30],
    [3.4, 20],
    [3.3, 10]
])


function callEndpoint() {
    $(".label").css("color", "red");
    Promise.resolve(
        $.ajax({
            url: prodPoint,
            timeout: 5000,//5 second timeout
        })
    ).then(function () {
        updateDepth(prodPoint)
    }).catch(function (e) {
        updateDepth(prodPoint)//CHANGE FOR TEST
    });
}

function goToApp() {
    clearInterval(interval);
    console.log('close')
}