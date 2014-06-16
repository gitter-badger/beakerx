;(function(app) {
  app.service('WindowMessageService', ['$window', '$location', '$rootScope', 'Factories', function($window, $location, $rootScope, Factories) {
    var F = Factories;

    function saveNotebook(data) {
      var createNotebook = _.partial(F.Notebooks.createNotebook, data.projectId);
      var saveFunction = data.operation == 'create' ?
        createNotebook : F.Notebooks.updateNotebook;
      saveFunction(data.notebook).
        then(function(notebook) {
          $rootScope.$broadcast('window-message-notebook-'+data.operation, notebook);
        }, function(response) {
          $window.alert("Error attempting to " + data.operation + " notebook.");
        });
    };

    function receiveWindowMessage(e) {
      if (new URL(event.origin).hostname !== $location.host()) {
        throw "message received from unauthorized host " + event.origin.host;
      }
      if (!e.data.notebook) return; // could be a message for a different purpose
      saveNotebook(e.data);
    }

    $window.addEventListener('message', receiveWindowMessage, false);

    // no public API
    return {};
  }]);
})(window.bunsen);
