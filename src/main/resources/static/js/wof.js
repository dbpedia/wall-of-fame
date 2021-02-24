app.controller('webIdController', function($scope, $http, $filter, $mdPanel, $mdDialog) {

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
    $scope.selectedOptions=[];

    this.showToolbarMenu = function($event, webId) {

        function PanelMenuCtrl(mdPanelRef) {
            this.closeMenu = function() {
                mdPanelRef && mdPanelRef.close();
            };
        }

        let escapedURL = escape(webId.url);

        let template = ''+
            '<div class="menu-panel" md-whiteframe="4" style="width: 180px">' +
            '  <div class="menu-content">' +
            '    <div class="menu-item">' +
            `      <a class="md-button" ng-href="/validator?webid=${escapedURL}" target="_blank">Validate</a>` +
            '    </div>' +
            '    <div class="menu-item">' +
            `      <a class="md-button" ng-href="${webId.url}" target="_blank">Download</a>` +
            '    </div>' +
            '    <md-divider></md-divider>' +
            '    <div class="menu-item">' +
            '      <button class="md-button" ng-click="panelCtrl.closeMenu()">' +
            '        <span>Close Menu</span>' +
            '      </button>' +
            '    </div>' +
            '  </div>' +
            '</div>';

        console.log(template);

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

    this.showFullCard = function($event, webId) {

        let img = webId.img || 'https://wiki.dbpedia.org/sites/default/files/DBpediaLogo.png';

        let template = '' +
            '<div id="opaque" ng-click="panelCtrl.closeMenu()"></div>' +
            '<md-card style="z-index: 1">\n' +
            `    <img id="big-img" class="md-card-image" ng-src=${img}>\n` +
            '    <md-card-title>\n' +
            '        <md-card-title-text>\n' +
            '           <span class="md-headline" style="text-align: center">\n' +
            `             <a href="https://databus.dbpedia.org/${webId.account}" target="_blank">${webId.account}</a>\n` +
            '           </span>\n' +
            '        </md-card-title-text>\n' +
            '    </md-card-title>\n' +
            '    <md-card-content>\n' +
            '        <table>\n' +
            '            <tbody>\n' +
            '            <tr>\n' +
            '                <td valign="top">Name:</td>\n' +
            `                <td>${webId.name}</td>\n` +
            '            </tr>\n' +
            `            <tr ng-if="${!!webId.gender}">\n` +
            `                <td valign="top">Gender:</td>\n` +
            `                <td>${webId.gender}</td>\n` +
            '            </tr>\n' +
            `            <tr ng-if=${!!webId.geekCode}>\n` +
            '                <td valign="top">GeekCode:</td>\n' +
            `                <td>${webId.geekCode}</td>\n` +
            '            </tr>\n' +
            '            </tbody>\n' +
            '        </table>\n' +
            '    </md-card-content>\n' +
            '     <md-card-actions layout="column" layout-align="start">' +
            `         <md-button className="md-icon-button" aria-label="Settings" > ` + //ng-click=${this.showToolbarMenu($event, webId)}
            '            <md-icon style="text-align: center" md-svg-icon="img/icons/menu.svg"></md-icon>' +
            '         </md-button>' +
            '     </md-card-actions>' +
            '</md-card>'

        let position = $mdPanel.newPanelPosition()
            .absolute()
            .center();

        function PanelMenuCtrl(mdPanelRef) {
            this.closeMenu = function() {
                document.getElementById("opaque").style.display='none';
                mdPanelRef && mdPanelRef.close();
            };
        }

        let config = {
            id: 'fullCard',
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
            propagateContainerEvents: true
        };

        $mdPanel.open(config);
    };

    $scope.request = $http.get("/webids.json", {headers: { 'Accept': 'application/json'}}).then(function(response) {

        $scope.webids = response.data.webIds;

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
            let optionObject = {
                title : option ,
                type: "options",
                options: optionList
            };
            $scope.filterOptions.push(optionObject);
        });

        $scope.selectedOptions = angular.copy($scope.filterOptions);
        $scope.shownWebIds = $scope.filterWebIdsBySelectedOptions();
    });


    $scope.filterWebIdsBySelectedOptions = function (){
        let filtered = [];

        angular.forEach($scope.webids, function (webid){
            let valid = true;
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
