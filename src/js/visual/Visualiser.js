goog.provide('visual.Visualiser');

visual.Visualiser = function(canvasId, gameHeight, gameWidth, paddleHeight, paddleWidth) {
  this.canvas = document.getElementById(canvasId);
  this.ctx = this.canvas.getContext("2d");

  this.gameHeight = gameHeight;
  this.gameWidth = gameWidth;
  this.paddleHeight = paddleHeight;
  this.paddleWidth = paddleWidth;
};

visual.Visualiser.prototype.clear = function() {
  this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
};

visual.Visualiser.prototype.drawPlayer = function(position) {
  this.drawPaddle(this.gameWidth, position);
};

visual.Visualiser.prototype.drawLeftOpponent = function(position) {
  this.drawPaddle(0, position);
};

visual.Visualiser.prototype.drawRightOpponent = function(position) {
  this.drawPaddle(this.gameWidth * 2, position);
};

visual.Visualiser.prototype.scaleX = function(gameX) {
  return gameX * (this.canvas.width / ((this.paddleWidth*2)+(this.gameWidth*2)));
};

visual.Visualiser.prototype.x = function(gameX) {
  return this.scaleX(this.paddleWidth/2 + gameX);
};

visual.Visualiser.prototype.scaleY = function(gameY) {
  return gameY * (this.canvas.height / this.gameHeight);
};

visual.Visualiser.prototype.y = function(gameY) {
  return this.canvas.height - this.scaleY(gameY);
};

visual.Visualiser.prototype.drawPaddle = function(gameX, position) {
  this.ctx.fillStyle = "#fff";

//  console.log(gameX);
  console.log(this.x(gameX));

  this.ctx.fillRect(
    this.x(gameX), 
    this.y(position + this.paddleHeight / 2),
    this.scaleX(this.paddleWidth),
    this.scaleY(this.paddleHeight));


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
