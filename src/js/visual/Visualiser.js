goog.provide('visual.Visualiser');

visual.Visualiser = function(canvasId, gameHeight) {
    this.canvas = document.getElementById(canvasId);
    this.gameHeight = gameHeight;
};

visual.Visualiser.prototype.update = function(data) {

    var ctx= this.canvas.getContext("2d");
    ctx.clearRect(0,0,this.canvas.width, this.canvas.height);

    this.drawPlayer(ctx, 10, this.y(data[0].player.position));
    this.drawPlayer(ctx, this.canvas.width/2, this.y(data[1].player.position));
    this.drawPlayer(ctx, this.canvas.width - 20, this.y(data[2].player.position));

    this.drawBall(ctx, 30, 40);
    this.drawBall(ctx, 50, 60);
    this.drawBall(ctx, 150, 200);
    this.drawBall(ctx, 450, 10);
};

visual.Visualiser.prototype.y = function(gameY) {
    var y = this.canvas.height - gameY * (this.canvas.height / this.gameHeight);
    return y;
};

visual.Visualiser.prototype.drawPlayer = function(ctx, x, y) {

    ctx.fillStyle="#fff";
    ctx.fillRect(x,y,10,50);

};

visual.Visualiser.prototype.drawBall = function(ctx, x, y) {

    ctx.fillStyle="#fff";
    ctx.fillRect(x,y,10,10);

};
