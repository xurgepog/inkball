package inkball;

import org.junit.jupiter.api.Test;


import processing.core.PApplet;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;

public class AppTest {

    private App app;

    @BeforeEach
    public void setUp() {
        // create a new instance of the game for each test case
        app = new App();
        app.noLoop();
        PApplet.runSketch(new String[] { "App" }, app);
        while (app.width == 0) { // Keep looping until width is set by Processing (indicating it's ready)
            Thread.yield();
        }
        app.setup();
        app.setBallCount = 1;
        Ball testBall = new Ball('0');
        app.balls = Arrays.copyOfRange(app.balls, 0, 1);
        app.balls[0] = testBall;
        app.delay(1000);        
    }

    @Test
    public void keyPresses() {
        // pause testing
        KeyEvent keyEvent = new KeyEvent(app, 0, 0, 0, ' ', 32);
        app.keyReleased(keyEvent);
        assertEquals(true, app.paused);
        app.keyReleased(keyEvent);
        assertEquals(false, app.paused);

        // reset testing
        keyEvent = new KeyEvent(app, 0, 0, 0, 'r', 114);
        app.keyReleased(keyEvent);
        assertEquals(0, app.elapsedTime);

        app.levelOver = true;
        keyEvent = new KeyEvent(app, 0, 0, 0, 'r', 114);
        app.keyReleased(keyEvent);
        assertEquals(0, app.elapsedTime);

        app.levelOver = true;
        app.levelToload = app.levelCount;
        keyEvent = new KeyEvent(app, 0, 0, 0, 'r', 114);
        app.keyReleased(keyEvent);
        assertEquals(0, app.elapsedTime);

        // keypress
        app.keyPressed(keyEvent);

        // ctrl
        app.levelOver = false;
        // press
        keyEvent = new KeyEvent(app, 0, 0, 0, '?', App.CONTROL);
        app.keyCode = App.CONTROL;
        app.keyPressed(keyEvent);

        // release
        keyEvent = new KeyEvent(app, 0, 0, 0, '?', App.CONTROL);
        app.keyCode = App.CONTROL;
        app.keyReleased(keyEvent);
    }

    @Test
    public void mousePresses() {
        // Nothing pressed
        MouseEvent mouseEvent = new MouseEvent(app, 0, 0, 0, 100, 100, 0, 0);
        app.mousePressed(mouseEvent);

        // Left mouse
        app.mouseX = 100;
        app.mouseY = 100;
        mouseEvent = new MouseEvent(app, 0, 0, 0, 100, 100, App.LEFT, 0);
        app.mouseButton = App.LEFT;
        app.mousePressed(mouseEvent);
        assertEquals(100, app.prevX);

        // Right mouse
        // No line near
        mouseEvent = new MouseEvent(app, 0, 0, 0, 100, 100, App.RIGHT, 0);
        app.mouseButton = App.RIGHT;
        app.mousePressed(mouseEvent);

        // On line
        Line line = new Line();
        line.addSegment(3*App.CELLSIZE, 2*App.CELLSIZE, 3*App.CELLSIZE, 5*App.CELLSIZE);

        app.lines.add(line);
        app.mouseX = 3*App.CELLSIZE;
        app.mouseY = 4*App.CELLSIZE;
        mouseEvent = new MouseEvent(app, 0, 0, 0, 3*App.CELLSIZE, 4*App.CELLSIZE, PApplet.RIGHT, 0);
        app.mousePressed(mouseEvent);
        assertEquals(1, app.lines.size());

        // game paused
        app.paused = true;
        app.mousePressed(mouseEvent);

        // level over
        app.levelOver = true;
        app.mousePressed(mouseEvent);
        app.paused = false;
        app.levelOver = false;

        // drag mouse
        mouseEvent = new MouseEvent(app, 0, 0, 0, 100, 100, App.LEFT, 0);
        app.mouseButton = App.LEFT;
        app.mouseDragged(mouseEvent);

        app.paused = true;
        mouseEvent = new MouseEvent(app, 0, 0, 0, 100, 100, App.LEFT, 0);
        app.mouseButton = App.LEFT;
        app.mouseDragged(mouseEvent);

        app.levelOver = true;
        mouseEvent = new MouseEvent(app, 0, 0, 0, 100, 100, App.LEFT, 0);
        app.mouseButton = App.LEFT;
        app.mouseDragged(mouseEvent);

        // mouse release
        app.mouseReleased(mouseEvent);
        assertEquals(null, app.line);
    }

    @Test
    public void ballMovement() {
        // ball placement
        Ball ball = new Ball('0');
        int spawnLocX = 3;
        int spawnLocY = 5;
        ball.spawn(spawnLocX, spawnLocY);
        assertEquals(spawnLocX + ball.spawnOffset, ball.x);

        // ball velocity
        ball.draw(app);
        assertEquals(spawnLocX + ball.spawnOffset + ball.xVel / App.CELLSIZE, ball.x);

        // ball wall bounce
        ball.xVel = -1;
        ball.yVel = 0;
        ball.x = 0.5f;
        ball.y = 5;
        ball.draw(app);
        assertEquals(1, ball.xVel);

        // ball change colour
        ball.x = 9;
        ball.y = 0.5f;
        ball.xVel = 0;
        ball.yVel = -1;
        ball.draw(app);
        assertEquals('2', ball.colour);

        // ball accelerate
        // right
        ball.x = 3;
        ball.y = 14;
        ball.xVel = 0;
        ball.yVel = 0;
        ball.draw(app);
        assertEquals(0.4f, ball.xVel);

        // left
        ball.x = 15;
        ball.y = 8;
        ball.xVel = 0;
        ball.yVel = 0;
        ball.draw(app);
        assertEquals(-0.4f, ball.xVel);
        
        // up
        ball.x = 15;
        ball.y = 14;
        ball.xVel = 0;
        ball.yVel = 0;
        ball.draw(app);
        assertEquals(-0.4f, ball.yVel);

        // down
        ball.x = 15;
        ball.y = 3;
        ball.xVel = 0;
        ball.yVel = 0;
        ball.draw(app);
        assertEquals(0.4f, ball.yVel);

        // avoiding detecting other numerical indicators as walls
        // hole
        ball.x = 12;
        ball.y = 5.5f;
        ball.xVel = 0;
        ball.yVel = 0;
        ball.draw(app);
        assertEquals(0, ball.xVel);

        // default ball (B#)
        ball.x = 7;
        ball.y = 13;
        ball.xVel = 0;
        ball.yVel = 0;
        ball.draw(app);
        assertEquals(0, ball.xVel);

        // ball trying to reflect off wall wrong check
        ball.x = 17.3f;
        ball.y = 12;
        ball.xVel = -0.1f;
        ball.wallCollisionCheck(app, 0, ball.radius, ball.xVel, -1);
        assertEquals(-0.1f, ball.xVel);
    }

    @Test
    public void ballSpawningAndScoring() { // fix
        // not close enough for anything
        Ball ball = new Ball('1');
        ball.spawn(11, 6);
        app.board[6][11].type = 'H';
        app.board[6][12].type = '0';
        app.board[7][11].type = 'E';
        app.board[6][12].type = 'E';
        ball.holeCollsionCheck(app, 11, 6);
        assertEquals(true, ball.spawned);

        // close enough for attraction - 1
        ball.holeCollsionCheck(app, 11.5f, 6.5f);
        assertEquals(true, ball.spawned);

        // close enough for attraction - 2
        ball.holeCollsionCheck(app, 11.65f, 6.65f);
        assertEquals(true, ball.spawned);

        // close enough for sink - wrong hole
        ball.holeCollsionCheck(app, 11.8f, 6.8f);
        assertEquals(false, ball.spawned);

        // close enough for sink - right hole
        app.delay(100);
        ball.x = 15;
        ball.y = 1;
        app.board[1][15].type = 'H';
        app.board[1][16].type = '0';
        app.board[2][15].type = 'E';
        app.board[2][16].type = 'E';
        ball.holeCollsionCheck(app, 15.8f, 1.8f);
        assertEquals(true, ball.sunk);

        // ball on E square
        ball.x = 15;
        ball.y = 2;
        ball.holeCollsionCheck(app, 16.5f, 2.5f);
    }

    @Test
    public void timeUp() {
        // elapsed time runs out
        app.handleTimeMessages(0, 0, false, 60, 25);
        app.handleTimeMessages(0, 0, true, 60, 25);
        app.handleTimeMessages(-1, 0, false, 60, 25);
    }

    @Test
    public void levelsComplete() {
        // level complete
        app.levelOver = true;
        app.levelOver(app.levelOver);
        app.elapsedTime = app.time + 1;
        app.levelOver(app.levelOver);
    }

    @Test
    public void line() {
        // drawing line
        Line line = new Line();
        // app.mouseButton = App.LEFT;
        line.addSegment(3*App.CELLSIZE, 2*App.CELLSIZE, 3*App.CELLSIZE, 5*App.CELLSIZE);
        line.addSegment(3*App.CELLSIZE, 5*App.CELLSIZE, 5*App.CELLSIZE, 7*App.CELLSIZE);
        line.draw(app);

        // right clicking off line
        PVector[] clickSegment = line.isNearLine(10*App.CELLSIZE, 10*App.CELLSIZE, 5);
        assertEquals(null, clickSegment);

        // right clicking on line
        clickSegment = line.isNearLine(3*App.CELLSIZE, 4*App.CELLSIZE, 5);
        assertEquals(line.lines.get(0), clickSegment);

        // ball bouncing off line
        app.lines.add(line);
        Ball ball = new Ball('0');
        ball.spawn(3, 2);
        ball.xVel = 1;
        ball.yVel = 0;
        ball.draw(app);
        assertEquals(-1, ball.xVel);
    }

    @Test
    public void drawLoop() {
        // default loop
        app.draw();

        // pause
        app.paused = true;
        app.draw();
        app.paused = false;

        // levelover
        app.levelOver = true;
        app.draw();
        app.levelOver = false;
    }
}