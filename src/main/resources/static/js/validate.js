app.controller('validateController', function($scope, $http, $filter) {

    $scope.webId = {
        url: "https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl",
        turtle: '@base <https://raw.githubusercontent.com/Eisenbahnplatte/eisenbahnplatte.github.io/master/webid.ttl> .\n' +
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
            '\n' +
            'cert:key [\n' +
            '    a cert:RSAPublicKey;\n' +
            '    rdfs:label "HP Elitebook";\n' +
            '    cert:modulus "C133F14349AC1035EC007228975FA276E52A7D4E2F227710D645C616E92666C861838AFF268491990F9C30F6999E2C62DF3379DA0FDCE300CF1BED6B37F25FF9ADD5BD242E346E1C25E33891A95BD9B998D177D389A163B150383FE6EE1D9F479B2F186EF0BB11B4E8AC87AEB2414BA653741E87E8E72A083D00C813B1242158FFC957089C97044241DBC9CAE553CEE5B869A3667596E4E6A34998CEE9A588617B54432010CCDCF5EC7C4140B6AA3422AB089E5676847F727DA8762D1BA35FA4F0593AF91BFFA5AA4B433C07F1982CA22F1BEB1B538C8890632608C04E4A4E9129C1AA4575BAAE9014E30C0D7A5F96D98BCB4C5D0C794A8B5A2A7D823ECC5411"^^xsd:hexBinary;\n' +
            '    cert:exponent "65537"^^xsd:nonNegativeInteger\n' +
            '] .',
        name: "Fabian",
        img: "https://eisenbahnplatte.github.io/eisenbahnplatte.jpeg",
        gender: "male",
        geekCode: "GMU GCS s: d? !a L++ PS+++ PE- G h",
        firstname: "Fabian"
    };

    $scope.result={
        result: ""
    };

    $scope.validateWebId = function() {

        $http({
            url: "/validate",
            method: "GET",
            headers: {'Content-Type': 'application/json'},
            params: {'str': $scope.webId.turtle}
        }).then(function (response){
            console.log(response.data);
            $scope.webId = angular.fromJson(response.data.WebId);
            $scope.result = angular.fromJson(response.data.Result);
        });

    }

    $scope.fetchAndValidateWebId = function() {

        if (validateURL($scope.webId.url).valueOf()) {
            $http({
                url: "/validate",
                method: "GET",
                headers: {'Content-Type': 'application/json'},
                params: {'str': $scope.webId.url}
            }).then(function (response){
                console.log(response.data);
                $scope.webId = angular.fromJson(response.data.WebId);
                $scope.result = angular.fromJson(response.data.Result);
            });
        }
    }
});



function validateURL(webIdURL) {

    if (webIdURL.toString().includes("https://")) {
        return true;
    } else {
        document.getElementById('url_ok').style.display = "block";
        return false;
    }
}
