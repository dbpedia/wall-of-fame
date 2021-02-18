app.controller('webIdController', function($scope, $http, $filter, $mdDialog) {

    $scope.model={};
    $scope.model.searchString = "";

    $scope.filterOptions = [
        {
            title: "img",
            type: "hasField",
            options: ["yes", "no"]
        },
        {
            title: "geekCode",
            type: "hasField",
            options: ["yes", "no"]
        }
    ];
    $scope.selectedOptions = [];

    // $scope.openOffscreen = function(webid) {
    //     $mdDialog.show(
    //         $mdDialog.alert()
    //             .clickOutsideToClose(true)
    //             .title('WebId')
    //             .textContent('Closing to offscreen')
    //             .ariaLabel('Offscreen Demo')
    //             .ok('Amazing!')
    //             // Or you can specify the rect to do the transition from
    //             .openFrom({
    //                 top: -50,
    //                 width: 30,
    //                 height: 80
    //             })
    //             .closeTo({
    //                 left: 1500
    //             })
    //     );
    // };

    $scope.request = $http.get("/webids.json", {headers: { 'Accept': 'application/json'}}).then(function(response) {

        $scope.webids = response.data.webIds;

        var options = [];
        angular.forEach($scope.webids, function(webid){
            angular.forEach(Object.keys(webid), function(key){
                if(options.indexOf(key) < 0) options.push(key);
            })
        });

        let optionsToBeIgnored = ["account", "url", "name", "maker", "img", "geekCode"];
        options = options.filter(item => !optionsToBeIgnored.includes(item));

        angular.forEach(options, function (option){
            var optionList = $filter('unique')($scope.webids, option);
            var optionObject = {
                title : option ,
                type: "options",
                options: optionList
            };
            $scope.filterOptions.push(optionObject);
        });

        $scope.selectedOptions = angular.copy($scope.filterOptions);
        $scope.filteredWebIds = $scope.filtered();
    });

    $scope.filtered = function (){
        var filtered = [];

        angular.forEach($scope.webids, function (webid){
            var valid = true;
            angular.forEach($scope.selectedOptions, function (option){
                if (option.type==="hasField"){
                    if (option.options.length===1) {
                        if (option.options.includes("no")) {
                            if(!(webid[option.title]===undefined)) valid = false;
                        }
                        if (option.options.includes("yes")) {
                            if(webid[option.title]===undefined) valid = false;
                        }
                    }
                } else {
                    if (option.options.length>0) {
                        if (!option.options.includes(webid[option.title])) valid = false;
                    }
                }
            });

            if (valid) filtered.push(webid);
        })

        return filtered;
    };


    $scope.toggle = function (toggledOption, item) {
        angular.forEach($scope.selectedOptions, function(option){
           if (option.title===toggledOption){
               if(option.options.includes(item)){
                   let index = option.options.indexOf(item);
                    option.options.splice(index,1);
               }else{
                   option.options.push(item);
               }
           }
        });

        $scope.filteredWebIds = $scope.filtered();
    };


    $scope.exists = function (checkedOption, item) {
        if ($scope.selectedOptions.length===0) return true;

        var exists = false;
        angular.forEach($scope.selectedOptions, function(option){
            if (option.title===checkedOption){
                if(option.options.includes(item)){
                    exists=true;
                }
            }
        });

        return exists;
    };


    $scope.isIndeterminate = function(opt) {
        var isIndeterminate = false

        angular.forEach($scope.selectedOptions, function(option){
            if (option.title===opt.title){
                isIndeterminate = (option.options.length !==0 && option.options.length !== opt.options.length);
            }});

        return isIndeterminate;
    };


    $scope.isChecked = function(opt) {
        var isChecked = false;

        angular.forEach($scope.selectedOptions, function(option){
            if (option.title===opt.title){
                isChecked = (option.options.length === opt.options.length);
            }});

        return isChecked;
    };


    $scope.toggleAll = function(opt) {
        angular.forEach($scope.selectedOptions, function(option){
            if (option.title===opt.title){
                if (option.options.length === opt.options.length) {
                    option.options = [];
                } else if (option.options.length === 0 || option.options.length > 0) {
                    option.options = angular.copy(opt.options);
                }
            }});
        $scope.filteredWebIds = $scope.filtered();
    };


});

/**
 * Filters out all duplicate items from an array by checking the specified key
 * @param [key] {string} the name of the attribute of each object to compare for uniqueness
 if the key is empty, the entire object will be compared
 if the key === false then no filtering will be performed
 * @return {array}
 */
app.filter('unique', function () {

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
                    newItems.push(item[filterOn]);
                }

            });
            items = newItems;
        }
        return items;
    };
});


// app.filter('filterWebIds', function ($filter) {
//    return function (input, filterArray){
//        if(filterArray.length===0){
//            return input;
//        }
//
//        var output = [];
//        filterArray.forEach(element => output = output.concat($filter('filter')(input, element)));
//
//        return $filter('unique')(output);
//    };
// });
//
// app.filter('isdefined', function ($filter) {
//     return function (input, key){
//         var output = [];
//         input.forEach(element => {
//             if(element.hasOwnProperty(key)) {
//                 output = output.concat(element);
//             }
//         });
//
//         return $filter('unique')(output);
//     };
// });
