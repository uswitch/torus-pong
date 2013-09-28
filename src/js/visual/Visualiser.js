goog.provide('visual.Visualiser');

visual.Visualiser = function(canvasId) {
    console.log(canvasId);
    this.canvas = document.getElementById(canvasId);
};

visual.Visualiser.prototype.update = function(data) {
    console.log("update");
};
