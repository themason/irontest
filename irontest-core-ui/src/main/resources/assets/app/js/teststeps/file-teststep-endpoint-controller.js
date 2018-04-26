'use strict';

//  NOTICE:
//    The $scope here prototypically inherits from the $scope of TeststepsEndpointController.
//    ng-include also creates a scope.
angular.module('irontest').controller('FILETeststepEndpointController', ['$scope',
  function($scope) {
    $scope.fileEndpointDestinationTypeChanged = function(isValid) {
      //  clear properties for Client connection mode, to avoid saving unintended values
      var endpointProperties = $scope.teststep.endpoint.otherProperties;
      endpointProperties.hostname = null;
      endpointProperties.port = null;
      endpointProperties.username = null;
      endpointProperties.password = null;

      if (!$scope.isInShareEndpointMode()) {
        //  save test step immediately (so as to update endpoint)
        $scope.update(isValid);
      }
    };
  }
]);
