app.controller('webIdController', function($scope, $http, $filter, $mdPanel, $mdDialog) {

    $scope.model={};
    $scope.model.searchString = "";

    $scope.filterOptions = [
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
    ];
    $scope.selectedOptions=[];

    $scope.shownWebIds = undefined;
    $scope.activeWebId = undefined;


    $scope.request = $http.get("/webids.json", {headers: { 'Accept': 'application/json'}}).then(function(response) {
        $scope.webids = response.data.webIds;
        console.log($scope.webids);
        let options = [];
        angular.forEach($scope.webids, function(webid){
            angular.forEach(Object.keys(webid), function(key){
                if(options.indexOf(key) < 0) options.push(key);
            })
        });
        let optionsToBeIgnored = ["account", "url", "name", "maker", "img", "geekCode"];
        options = options.filter(item => !optionsToBeIgnored.includes(item));

        angular.forEach(options, function (option){
            let optionList = $filter('unique')($scope.webids, option);

            console.log(optionList);

            if (typeof optionList !== 'undefined' && optionList.length > 0) {
                // the array is defined and has at least one element
                let optionObject = {
                    title : option ,
                    type: "options",
                    values: optionList
                };

                switch (typeof optionList[0]){
                    case "bigint":
                    case "number": optionObject ={
                        title : option ,
                        type: "range",
                        values: getMinAndMax(optionList)
                    };
                        break;
                }

                $scope.filterOptions.push(optionObject);
            }

        });

        $scope.selectedOptions = angular.copy($scope.filterOptions);
        $scope.shownWebIds = $scope.filterWebIdsBySelectedOptions();
    });

    $scope.showFullCard = function (webid) {
        $scope.activeWebId = webid;
        console.log(webid);
        document.getElementById("full_webid_container").style.display='block';
    };

    $scope.hideFullCard = function(){
        document.getElementById("full_webid_container").style.display='none';
    };


    $scope.getIndexOfStrInSelectedOptions = function(str) {
        return $scope.selectedOptions.map(function (e){return e.title;}).indexOf(str);
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
            angular.forEach($scope.selectedOptions, function (option){
                if (option.type==="boolean"){
                    if (option.values.includes("true")) {
                        if(webid[option.title]===undefined) valid = false;
                    }
                } else if(option.type==="range") {
                    if (option.values.length>0) {
                        if(webid[option.title] < option.values[0] || webid[option.title] > option.values[1]) valid = false;
                    }
                }
                else {
                    if (option.values.length>0) {
                        if (!option.values.includes(webid[option.title])) valid = false;
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


    this.showToolbarMenu = function($event, webId) {

        function PanelMenuCtrl(mdPanelRef) {
            this.closeMenu = function() {
                mdPanelRef && mdPanelRef.close();
            };
        }

        let escapedURL = escape(webId.url);

        let template = ''+
            '<div class="menu-panel" md-whiteframe="4" ng-mouseleave="panelCtrl.closeMenu()">' +
            '  <div class="menu-content">' +
            '    <div class="menu-item">' +
            `      <a class="md-button" ng-href="/validator?webid=${escapedURL}" target="_blank">Validate</a>` +
            '    </div>' +
            '    <md-divider></md-divider>' +
            '    <div class="menu-item">' +
            `      <a class="md-button" ng-href="${webId.url}" target="_blank">Download</a>` +
            '    </div>' +
            '  </div>' +
            '</div>';

        let position = $mdPanel.newPanelPosition()
            .relativeTo($event.target)
            .addPanelPosition(
                $mdPanel.xPosition.ALIGN_START,
                $mdPanel.yPosition.BELOW
            );

        let config = {
            id: 'toolbar',
            attachTo: angular.element(document.body),
            controller: PanelMenuCtrl,
            controllerAs: 'panelCtrl',
            template: template,
            position: position,
            panelClass: 'menu-panel-container',
            locals: {} ,
            openFrom: $event,
            focusOnOpen: false,
            zIndex: 100,
            propagateContainerEvents: true,
            groupName: ['toolbar', 'menus']
        };

        $mdPanel.open(config);
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
        array.sort((a,b)=> a.title.localeCompare(b.title));
        return array.sort((a,b)=> b.type.localeCompare(a.type));
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
