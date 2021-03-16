app.controller('webIdController', function($scope, $http, $filter, $mdPanel, $mdDialog) {

    $scope.model={};
    $scope.model.searchString = "";

    $scope.filterOptions = {
        databus: [],
        others: [
            {
                title: "img",
                type: "boolean",
                values: []
            },
            {
                title: "geekCode",
                type: "boolean",
                values: []
            }
        ]
    };

    let labelMap ={
        "img": "Image",
        "geekCode": "Geek Code",
        "numArtifacts": "Artifacts",
        "numVersions" : "Versions",
        "uploadSize": "Upload Size (MB)"
    }

    $scope.selectedOptions={};

    $scope.shownWebIds = undefined;
    $scope.activeWebId = undefined;
    $scope.currentCategory = undefined;

    $scope.request = $http.get("/webids.json", {headers: { 'Accept': 'application/json'}}).then(function(response) {
        $scope.webids = response.data.webIds;
        console.log($scope.webids);

        let optionsToBeIgnored = ["account", "url", "name", "maker", "person", "img", "geekCode"];
        // let options = [];

        angular.forEach($scope.webids, function(webid){
                angular.forEach(Object.keys(webid), function (category){
                    angular.forEach(Object.keys(webid[category]), function (property){
                        if(!optionsToBeIgnored.includes(property)) {
                            // insert select option, if does not exist yet.
                            if(!$scope.filterOptions[category]){
                                $scope.filterOptions[category] = [];
                            }
                            if(!$scope.filterOptions[category].some(e => e.title === property)) {
                                let thisType;
                                switch (typeof webid[category][property]) {
                                    case "bigint":
                                    case "number":
                                        thisType = "range";
                                        break;
                                    default:
                                        thisType = "options";
                                }

                                $scope.filterOptions[category].push({
                                    title: property,
                                    type: thisType,
                                    values: []
                                });
                            }

                            // insert all values that dont exist yet
                            let index = $scope.filterOptions[category].findIndex(x => x.title === property)
                            if(!$scope.filterOptions[category][index].values.includes(webid[category][property])){
                                $scope.filterOptions[category][index].values.push(webid[category][property]);
                            }
                        }
                    });
                });
        });


        //sort options and only leave highest and lowest value of options of type "range"
        angular.forEach(Object.keys($scope.filterOptions), function(category){
            $filter('sortSelectors')($scope.filterOptions[category]);
            angular.forEach($scope.filterOptions[category], function (option){
                if (option.type === "range") {
                    option.values = getMinAndMax(option.values)
                }
                option.label = labelMap[option.title];
            });
        });

        console.log("FilterOptions")
        console.log($scope.filterOptions);

        $scope.selectedOptions = angular.copy($scope.filterOptions);
        $scope.shownWebIds = $scope.filterWebIdsBySelectedOptions();

        // console.log("hallo");
        // options = options.filter(item => !optionsToBeIgnored.includes(item));

        // angular.forEach(options, function (option){
        //     console.log("JETZT")
        //     console.log(option.category)
        //     let values = $filter('unique')($scope.webids.map(a => a[option.category]), + option.title);
        //
        //
        //     console.log(values);
        //
        //     if (typeof values !== 'undefined' && values.length > 0) {
        //         // the array is defined and has at least one element
        //         let optionObject = {
        //             title : option.title ,
        //             category: option.category,
        //             type: "options",
        //             values: values
        //         };
        //
        //         switch (typeof values[0]){
        //             case "bigint":
        //             case "number": optionObject ={
        //                 title : option ,
        //                 category: option.category,
        //                 type: "range",
        //                 values: getMinAndMax(values)
        //             };
        //                 break;
        //         }
        //
        //         $scope.filterOptions.push(optionObject);
        //     }
        //
        // });
    });

    $scope.checkIfCategoryChanged = function(category, title) {
        let same = category.localeCompare($scope.currentCategory);
        console.log(title + " " + category + " is " + (same !== 0) + " of " + $scope.currentCategory)
        if(same !==0) $scope.currentCategory = category;
        return same !== 0;
    };

    $scope.showFullCard = function (webid) {
        $scope.activeWebId = webid;
        console.log(webid);
        document.getElementById("full_webid_container").style.display='block';
    };

    $scope.hideFullCard = function(){
        document.getElementById("full_webid_container").style.display='none';
    };


    $scope.getIndexOfStrInSelectedOptions = function(category, str) {
        return $scope.selectedOptions[category].map(function (e){return e.title;}).indexOf(str);
    };

    function getMinAndMax(arr) {
        let max = Math.max(...arr);
        let min = Math.min(...arr);
        let result = [min, max];
        return result;
    }

    $scope.updateShownWebIds = function (){
        $scope.shownWebIds = $scope.filterWebIdsBySelectedOptions();
    };

    $scope.filterWebIdsBySelectedOptions = function (){
        let filtered = [];

        angular.forEach($scope.webids, function (webid){
            let valid = true;
            angular.forEach(Object.keys($scope.selectedOptions), function (category){
                angular.forEach($scope.selectedOptions[category],function (option){
                    if (option.type==="boolean"){
                        if (option.values.includes("true")) {
                            if(webid[category][option.title]===undefined) valid = false;
                        }
                    } else if(option.type==="range") {
                        if (option.values.length>0) {
                            if(webid[category][option.title] < option.values[0] || webid[category][option.title] > option.values[1]) valid = false;
                        }
                    }
                    else {
                        if (option.values.length>0) {
                            if (!option.values.includes(webid[category][option.title])) valid = false;
                        }
                    }
                });
            });

            if (valid) filtered.push(webid);
        })

        return filtered;
    };


    $scope.toggle = function (category, toggledOption, item) {
        angular.forEach($scope.selectedOptions[category], function(option){
           if (option.title===toggledOption){
               if(option.values.includes(item)){
                   let index = option.values.indexOf(item);
                    option.values.splice(index,1);
               }else{
                   option.values.push(item);
               }
           }
        });

        console.log($scope.selectedOptions);

        $scope.shownWebIds = $scope.filterWebIdsBySelectedOptions();
    };


    $scope.exists = function (checkedOption, item) {
        if ($scope.selectedOptions.length===0) return true;

        let exists = false;
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
        let isIndeterminate = false

        angular.forEach($scope.selectedOptions, function(option){
            if (option.title===opt.title){
                isIndeterminate = (option.options.length !==0 && option.options.length !== opt.options.length);
            }});

        return isIndeterminate;
    };


    $scope.isChecked = function(opt) {
        let isChecked = false;

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
        $scope.shownWebIds = $scope.filterWebIdsBySelectedOptions();
    };

    $scope.childClick = function($event) {
        $event.stopPropagation();
    }

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
            let hashCheck = {}, newItems = [];
            console.log("NOW")
            console.log(filterOn)

            let extractValueToCompare = function (item) {
                if (angular.isObject(item) && angular.isString(filterOn)) {
                    return item[filterOn];
                } else {
                    return item;
                }
            };

            angular.forEach(items, function (item) {
                let valueToCheck, isDuplicate = false;

                for (let i = 0; i < newItems.length; i++) {
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

app.filter('escape', function() {
    return window.encodeURIComponent;
});

app.filter('sortSelectors', function() {
    return function (array) {
        return array.sort((a,b)=> b.type.localeCompare(a.type) || a.title.localeCompare(b.title));
    };
});

(function() {
    let start = new Date;
    start.setHours(8, 0, 0); // 11pm

    function pad(num) {
        return ("0" + parseInt(num)).substr(-2);
    }

    function tick() {
        let now = new Date;
        if (now > start) { // too late, go to tomorrow
            start.setDate(start.getDate() + 1);
        }
        let remain = ((start - now) / 1000);
        let hh = pad((remain / 60 / 60) % 60);
        let mm = pad((remain / 60) % 60);
        let ss = pad(remain % 60);
        document.getElementById('time').innerHTML =
            hh + "h " + mm + "m " + ss + "s";
        setTimeout(tick, 1000);
    }

    document.addEventListener('DOMContentLoaded', tick);
})();

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


// this.showToolbarMenu = function($event, webId) {
//
//     function PanelMenuCtrl(mdPanelRef) {
//         this.closeMenu = function() {
//             mdPanelRef && mdPanelRef.close();
//         };
//     }
//
//     let escapedURL = escape(webId.url);
//
//     let template = ''+
//         '<div class="menu-panel" md-whiteframe="4" ng-mouseleave="panelCtrl.closeMenu()">' +
//         '  <div class="menu-content">' +
//         '    <div class="menu-item">' +
//         `      <a class="md-button" ng-href="/validator?webid=${escapedURL}" target="_blank">Validate</a>` +
//         '    </div>' +
//         '    <md-divider></md-divider>' +
//         '    <div class="menu-item">' +
//         `      <a class="md-button" ng-href="${webId.url}" target="_blank">Download</a>` +
//         '    </div>' +
//         '  </div>' +
//         '</div>';
//
//     let position = $mdPanel.newPanelPosition()
//         .relativeTo($event.target)
//         .addPanelPosition(
//             $mdPanel.xPosition.ALIGN_START,
//             $mdPanel.yPosition.BELOW
//         );
//
//     let config = {
//         id: 'toolbar',
//         attachTo: angular.element(document.body),
//         controller: PanelMenuCtrl,
//         controllerAs: 'panelCtrl',
//         template: template,
//         position: position,
//         panelClass: 'menu-panel-container',
//         locals: {} ,
//         openFrom: $event,
//         focusOnOpen: false,
//         zIndex: 100,
//         propagateContainerEvents: true,
//         groupName: ['toolbar', 'menus']
//     };
//
//     $mdPanel.open(config);
// };