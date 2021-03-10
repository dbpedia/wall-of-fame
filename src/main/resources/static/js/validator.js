app.controller('validateController', function($scope, $http, $filter) {
    $scope.webId = {
        maker: "https://eisenbahnplatte.github.io/webid.ttl#this",
        url: "",
        account: "",
        turtle: "",
        name: "",
        img: "",
        gender: "",
        geekCode: "",
        validation: {}
    };
    $scope.result={
        result: ""
    };

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("webid")) {
        $scope.webId.maker = urlParams.get("webid");
    }

    $scope.validateWebId = function (param = $scope.webId.turtle) {
        $http({
            url: "/validate",
            method: "GET",
            headers: {'Content-Type': 'application/json'},
            params: {'webid': param}
        }).then(function (response){
            $scope.webId = angular.fromJson(response.data);
            console.log($scope.webId);
        });
    };

    $scope.fetchAndValidateWebId = function() {
        if (validateURL($scope.webId.maker).valueOf()) {
            $scope.validateWebId($scope.webId.maker);
        }
    };

    $scope.fetchAndValidateWebId();

    console.log($scope.webId.account);

    // $scope.fetchAndValidateWebId = function(url) {
    //
    //     if (validateURL(url).valueOf()) {
    //         $http({
    //             url: "/validate",
    //             method: "GET",
    //             headers: {'Content-Type': 'application/json'},
    //             params: {'webid': url}
    //         }).then(function (response){
    //             console.log(response.data);
    //             $scope.webId = angular.fromJson(response.data.WebId);
    //             $scope.result = angular.fromJson(response.data.Result);
    //         });
    //     }
    // }


});



function validateURL(webIdURL) {

    if (webIdURL.toString().includes("https://")) {
        return true;
    } else {
        document.getElementById('url_ok').style.display = "block";
        return false;
    }
}
