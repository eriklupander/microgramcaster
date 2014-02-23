var cmd = new function() {
    this.id = '';
    this.params = {};

    this.parse = function(jsonStr) {
        return JSON.parse(jsonStr);
    }
};