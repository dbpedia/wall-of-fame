app.controller('validateController', function($scope, $http, $filter) {
    $scope.activeWebId = {
        general : {
            person: "https://eisenbahnplatte.github.io/webid.ttl#this",
            turtle: ""
        }
    };

    $scope.result={
        result: ""
    };

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("webid")) {
        $scope.activeWebId.general.person = urlParams.get("webid");
    }

    $scope.validateWebId = function (param = $scope.activeWebId.general.turtle) {
        $http({
            url: "/validate",
            method: "GET",
            headers: {'Content-Type': 'application/json'},
            params: {'webid': param}
        }).then(function (response){
            $scope.activeWebId = angular.fromJson(response.data);
            console.log($scope.activeWebId);
        });
    };

    $scope.fetchAndValidateWebId = function() {
        if (validateURL($scope.activeWebId.general.person).valueOf()) {
            $scope.validateWebId($scope.activeWebId.general.person);
        }
    };

    $scope.fetchAndValidateWebId();
});



function validateURL(webIdURL) {

    if (webIdURL.toString().includes("https://")) {
        return true;
    } else {
        document.getElementById('url_ok').style.display = "block";
        return false;
    }
}
