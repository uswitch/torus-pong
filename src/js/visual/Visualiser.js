goog.provide('visual.Visualiser');

visual.Visualiser = function(canvasId, gameHeight, paddleHeight, paddleWidth) {
  this.canvas = document.getElementById(canvasId);
  this.ctx = this.canvas.getContext("2d");

  this.gameHeight = gameHeight;
  this.paddleHeight = paddleHeight;
  this.paddleWidth = paddleWidth;
};

visual.Visualiser.prototype.clear = function() {
  this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
};

visual.Visualiser.prototype.drawPlayer = function(position) {
  this.drawPaddle(this.canvas.width / 2, position);
};

visual.Visualiser.prototype.drawLeftOpponent = function(position) {
  this.drawPaddle(0, position);
};

visual.Visualiser.prototype.drawRightOpponent = function(position) {
  this.drawPaddle(this.canvas.width - 20, position);
};



visual.Visualiser.prototype.scale = function(gameY) {
  return gameY * (this.canvas.height / this.gameHeight);
};


visual.Visualiser.prototype.y = function(gameY) {
  return this.canvas.height - this.scale(gameY);
};

visual.Visualiser.prototype.drawPaddle = function(x, position) {
  this.ctx.fillStyle = "#fff";
  this.ctx.fillRect(x, this.y(position + this.paddleHeight / 2),
                10, this.scale(this.paddleHeight));


};

visual.Visualiser.prototype.drawBall = function(ctx, x, y) {

  ctx.fillStyle="#fff";
  ctx.fillRect(x,y,10,10);

};


visual.Visualiser.prototype.update = function(data) {

    //var ctx= this.canvas.getContext("2d");

  // this.drawPlayer(ctx, 10, data[0].player.position);
  // this.drawPlayer(ctx, this.canvas.width/2, data[1].player.position);
  // this.drawPlayer(ctx, this.canvas.width - 20, data[2].player.position);

  // this.drawBall(ctx, 30, 40);
  // this.drawBall(ctx, 50, 60);
  // this.drawBall(ctx, 150, 200);
  // this.drawBall(ctx, 450, 10);
};
