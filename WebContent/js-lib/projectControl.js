var main = angular.module("main", ["ui.router"]);

var agentURL = "http://localhost:8080"+"/FinalProject-Team1/rest";
var stackURL = "https://cs.stackexchange.com/questions/";

//Clock on Index Page
main.directive('clock', ['dateFilter', '$timeout', function(dateFilter, $timeout){
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



main.config(function($stateProvider,$urlRouterProvider){
	
	$urlRouterProvider.otherwise("/");
	
	$stateProvider
		.state('Query',{
			url:"/query?q",
			controller:"resultController as rc",
			templateUrl:"web/display-template.html"
		})
		.state('Help',{
			url:"help",
			templateUrl:"web/helppage.html",
		});
	
	
})


main.service('httpService',function($rootScope,$q,$http,$timeout){
	this.queryResult = function(queryStr) {
		var deferred = $q.defer();
		return $http.get('web/testResult.json')//(agentUrl+'/query'+queryStr)
				.then(function (response){
				deferred.resolve(response.data);
				return deferred.promise;
			}, function(response){
				deferred.reject(response);
				return deferred.promise;
			})
	}
})


main.controller('QueryController', ['$state', '$scope', '$stateParams', function($state, $scope, $stateParams) {
	
	$scope.search = $stateParams.q;	
    $scope.query = function() {
		console.log("Btn Clicked!")
      console.log($scope.search)
      $state.go('Query', { q: $scope.search });
    }
  }]);


main.controller('resultController',['$state', '$scope', '$stateParams','httpService',function($state, $scope, $stateParams,httpService){
	console.log($stateParams.q)
	$scope.query = $stateParams.q;
	$scope.stackURL  = stackURL;
	httpService.queryResult($stateParams.q).then(function(result){
		$scope.resultsSize = result.length;
		$scope.queryResults = result;
	})
	
	
	
}])


