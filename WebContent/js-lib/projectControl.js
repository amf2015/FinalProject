var mainControl = angular.module("mainControl", ["ui.router","ui.bootstrap"]);

var agentURL = "http://localhost:8080"+"/FinalProject-Team1/rest";

//Clock on Index Page
mainControl.directive('clock', ['dateFilter', '$timeout', function(dateFilter, $timeout){
    return {
        restrict: 'E',
        scope: {
            format: '@'
        },
        link: function(scope, element, attrs){
            var updateTime = function(){
                var now = Date.now();
                element.html(dateFilter(now, scope.format));
                $timeout(updateTime, now % 1000);
            };
            updateTime();
        }
    };
}]);


mainControl.service('httpService',function($rootScope,$q,$http,$timeout){
	this.queryResult = function(queryStr) {
		var deferred = $q.defer();
		return $http.get(agentUrl+'/query'+queryStr)
				.then(function (response){
				deferred.resolve(response.data);
				return deferred.promise;
			}, function(response){
				deferred.reject(response);
				return deferred.promise;
			})
	}
	
})
