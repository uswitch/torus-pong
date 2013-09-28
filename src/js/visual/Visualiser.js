goog.provide('visual.Visualiser');

visual.Visualiser = function(canvasId, gameHeight, playerHeight) {
    this.canvas = document.getElementById(canvasId);
    this.gameHeight = gameHeight;
    this.playerHeight = playerHeight;
};

visual.Visualiser.prototype.update = function(data) {

    var ctx= this.canvas.getContext("2d");
    ctx.clearRect(0,0,this.canvas.width, this.canvas.height);

    this.drawPlayer(ctx, 10, data[0].player.position);
    this.drawPlayer(ctx, this.canvas.width/2, data[1].player.position);
    this.drawPlayer(ctx, this.canvas.width - 20, data[2].player.position);

    this.drawBall(ctx, 30, 40);
    this.drawBall(ctx, 50, 60);
    this.drawBall(ctx, 150, 200);
    this.drawBall(ctx, 450, 10);
};

visual.Visualiser.prototype.scale = function(gameY) {
    return gameY * (this.canvas.height / this.gameHeight);
};

visual.Visualiser.prototype.y = function(gameY) {
    return this.canvas.height - this.scale(gameY);
};

visual.Visualiser.prototype.drawPlayer = function(ctx, x, y) {

    ctx.fillStyle="#fff";
    ctx.fillRect(x, this.y(y + this.playerHeight / 2),
                 10, this.scale(this.playerHeight));

};

visual.Visualiser.prototype.drawBall = function(ctx, x, y) {

    ctx.fillStyle="#fff";
    ctx.fillRect(x,y,10,10);

};
