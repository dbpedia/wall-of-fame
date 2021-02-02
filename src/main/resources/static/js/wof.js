var app = angular.module('wofApp', ['ngMaterial', 'ngMessages', 'material.svgAssetsCache']);
app.controller('webIdController', function($scope, $http) {
    $http.get("/webids.json").then(function(response) {
        $scope.webids = response.data.webIds;
    });
});