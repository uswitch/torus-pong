goog.provide('visual.Visualiser');

visual.Visualiser = function(canvasId) {
    this.canvas = document.getElementById(canvasId);
};

visual.Visualiser.prototype.update = function(data) {

    var ctx= this.canvas.getContext("2d");
    ctx.clearRect(0,0,this.width,this.height);

    this.drawPlayer(ctx, 10, 10);
    this.drawPlayer(ctx, this.canvas.width - 10, 50);
    this.drawPlayer(ctx, this.canvas.width/2, 25);

    this.drawBall(ctx, 30, 40);
    this.drawBall(ctx, 50, 60);
    this.drawBall(ctx, 150, 200);
    this.drawBall(ctx, 450, 10);
};

visual.Visualiser.prototype.drawPlayer = function(ctx, x, y) {

    ctx.fillStyle="#fff";
    ctx.fillRect(x,y,10,50);

};

visual.Visualiser.prototype.drawBall = function(ctx, x, y) {

    ctx.fillStyle="#fff";
    ctx.fillRect(x,y,10,10);

};
