var app = angular.module('wofApp', ['ngMaterial', 'ngMessages', 'material.svgAssetsCache']);


app.controller('webIdController', function($scope, $http, $filter) {

    $scope.request = $http.get("/webids.json").then(function(response) {
        $scope.webids = response.data.webIds;

        $scope.model={};
        $scope.model.searchString = "";
        //variables for gender selection
        $scope.filteredByGender = $filter('unique')($scope.webids, 'gender');
        $scope.genderOptions = [];
        for(var i=0; i < $scope.filteredByGender.length; i++){
          $scope.genderOptions.push($scope.filteredByGender[i].gender);
        }
        $scope.selectedGender = [];

        //variables for image selection
        $scope.imgOptions = ["yes", "no"];
        $scope.selectedIMG = [];


        $scope.toggle = function (item, list) {
            var idx = list.indexOf(item);
            if (idx > -1) {
                list.splice(idx, 1);
            }
            else {
                list.push(item);
            }

            // $scope.filteredWebIds = $filter('filterWithArray')($scope.webids, $scope.selectedGender);
            $scope.filteredWebIds = $scope.filtered()
            console.log($scope.model.searchString)
        };

        $scope.filtered = function (){
            var filtered=$filter('filterWithArray')($scope.webids, $scope.selectedGender);

            console.log("test");
            // var newar = out.filter(x => filtered.includes(x));
            if($scope.selectedIMG.length===0){
                return [];
            }
            if($scope.selectedIMG.includes("yes") && $scope.selectedIMG.length===1) {
                filtered = $filter('isdefined')(filtered, "img");
            }
            if($scope.selectedIMG.includes("no") && $scope.selectedIMG.length===1) {
                var oppositeFiltered = $filter('isdefined')(filtered, "img");
                console.log(oppositeFiltered);

                filtered = filtered.filter(x => !oppositeFiltered.includes(x));
            }

            console.log($scope.selectedGender);
            console.log($scope.selectedIMG);
            console.log(filtered);

            return filtered;
        }

        $scope.exists = function (item, list) {
            return list.indexOf(item) > -1;
        };

        $scope.isIndeterminate = function(sel, opt) {
            return ($scope[sel].length !== 0 &&
                $scope[sel].length !== opt.length);
        };

        $scope.isChecked = function(sel, opt) {
            return $scope[sel].length === opt.length;
        };

        $scope.toggleAll = function(sel, opt) {
            if ($scope[sel].length === opt.length) {
                $scope[sel] = [];
            } else if ($scope[sel].length === 0 || $scope[sel].length > 0) {
                $scope[sel] = opt.slice(0);
            }

            console.log($scope[sel]);

            // $scope.filteredWebIds = $filter('filterWithArray')($scope.webids, $scope[sel]);
            $scope.filteredWebIds = $scope.filtered()
        };
    });
});

/**
 * Filters out all duplicate items from an array by checking the specified key
 * @param [key] {string} the name of the attribute of each object to compare for uniqueness
 if the key is empty, the entire object will be compared
 if the key === false then no filtering will be performed
 * @return {array}
 */
angular.module('wofApp').filter('unique', function () {

    return function (items, filterOn) {

        if (filterOn === false) {
            return items;
        }

        if ((filterOn || angular.isUndefined(filterOn)) && angular.isArray(items)) {
            var hashCheck = {}, newItems = [];

            var extractValueToCompare = function (item) {
                if (angular.isObject(item) && angular.isString(filterOn)) {
                    return item[filterOn];
                } else {
                    return item;
                }
            };

            angular.forEach(items, function (item) {
                var valueToCheck, isDuplicate = false;

                for (var i = 0; i < newItems.length; i++) {
                    if (angular.equals(extractValueToCompare(newItems[i]), extractValueToCompare(item))) {
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    newItems.push(item);
                }

            });
            items = newItems;
        }
        return items;
    };
});


angular.module('wofApp').filter('filterWithArray', function ($filter) {
   return function (input, filterArray){

       var output = [];
       filterArray.forEach(element => output = output.concat($filter('filter')(input, element)));

       return $filter('unique')(output);
   };
});

angular.module('wofApp').filter('isdefined', function ($filter) {
    return function (input, key){
        var output = [];
        input.forEach(element => {
            if(element.hasOwnProperty(key)) {
                output = output.concat(element);
            }
        });

        return $filter('unique')(output);
    };
});

// angular.module('wofApp').filter('filterWithMultipleArrays', function ($filter) {
//     return function (input, filterArray){
//
//
//         var output = [];
//         filterArray.forEach(element => output = output.concat($filter('filter')(input, element)));
//
//         return $filter('unique')(output);
//     };
// });