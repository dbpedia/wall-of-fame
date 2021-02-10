app.controller('validateController', function($scope, $http, $filter) {

    $scope.webidStr='@base <https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl> .\n' +
        '@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n' +
        '@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n' +
        '@prefix cert: <http://www.w3.org/ns/auth/cert#> .\n' +
        '@prefix rdfs: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n' +
        '\n' +
        '<> a foaf:PersonalProfileDocument ;\n' +
        '    foaf:maker <#this> ;\n' +
        '    foaf:primaryTopic <#this> .\n' +
        '\n' +
        '<#this> a foaf:Person ;\n' +
        '     foaf:name "Eisenbahnplatte";\n' +
        '     foaf:img <https://eisenbahnplatte.github.io/eisenbahnplatte.jpeg>;\n' +
        '     foaf:gender "male";\n' +
        '     foaf:geekcode "GMU GCS s: d? !a L++ PS+++ PE- G h";\n' +
        '     foaf:firstname "Fabian";\n' +
        '\n' +
        'cert:key [\n' +
        '    a cert:RSAPublicKey;\n' +
        '    rdfs:label "HP Elitebook";\n' +
        '    cert:modulus "C133F14349AC1035EC007228975FA276E52A7D4E2F227710D645C616E92666C861838AFF268491990F9C30F6999E2C62DF3379DA0FDCE300CF1BED6B37F25FF9ADD5BD242E346E1C25E33891A95BD9B998D177D389A163B150383FE6EE1D9F479B2F186EF0BB11B4E8AC87AEB2414BA653741E87E8E72A083D00C813B1242158FFC957089C97044241DBC9CAE553CEE5B869A3667596E4E6A34998CEE9A588617B54432010CCDCF5EC7C4140B6AA3422AB089E5676847F727DA8762D1BA35FA4F0593AF91BFFA5AA4B433C07F1982CA22F1BEB1B538C8890632608C04E4A4E9129C1AA4575BAAE9014E30C0D7A5F96D98BCB4C5D0C794A8B5A2A7D823ECC5411"^^xsd:hexBinary;\n' +
        '    cert:exponent "65537"^^xsd:nonNegativeInteger\n' +
        '] .';


    $scope.sendPost = function() {
        console.log($scope.webidStr)
        $http.post("/validateWebId", $scope.webidStr).subscribe(
            data => {
                //apparently following line is not needed.
                //this.router.navigateByUrl(this.url);
                window.location.href = this.url;
            },
            error => {
                console.log("Error", error, this.params);
                this.data = error;
            },
            () => {
                console.log("POST is completed");
            });
    }
});

var coll = document.getElementsByClassName("collapsible");
var i;

for (i = 0; i < coll.length; i++) {
    coll[i].addEventListener("click", function () {
        this.classList.toggle("active");
        var content = this.nextElementSibling;
        if (content.style.display === "block") {
            content.style.display = "none";
        } else {
            content.style.display = "block";
        }
    });
}

function validateURL() {
    var a = document.getElementById('webidURL').value;
    if (a.toString().includes("https://")) {
        return true;
    } else {
        document.getElementById('url_ok').style.display = "block"
        return false;
    }
}
// window.addEventListener("load", function(){
//     // Dummy Array
//     var data2 = $("#result").val();
//     var data = ["doge", "cate", "birb", "doggo", "moon moon", "awkward seal"];
//
//     // Draw HTML table
//     var perrow = 3, // 3 cells per row
//         count = 0, // Flag for current cell
//         table = document.createElement("table"),
//         row = table.insertRow();
//
//     for (var i of data) {
//         var cell = row.insertCell();
//         cell.innerHTML = i;
//
//         /* You can also attach a click listener if you want
//         cell.addEventListener("click", function(){
//           alert("FOO!");
//         });
//         */
//
//         // Break into next row
//         count++;
//         if (count%perrow==0) {
//             row = table.insertRow();
//         }
//     }
//
//     // Attach table to container
//     document.getElementById("container").appendChild(table);
// });