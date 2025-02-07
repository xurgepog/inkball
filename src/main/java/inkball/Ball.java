package inkball;

import processing.core.PImage;
import processing.core.PVector;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a ball in the Inkball game.
 * 
 * A Ball has a color and a position on the game board. It can move 
 * in response to user input and game physics, and it interacts 
 * with tiles and holes on the board. Balls can be spawned at specific 
 * locations and can change their behavior based on the tiles they 
 * encounter.
 * 
 * Responsibilities of the Ball class include:
 * - Managing the position, velocity, and radius of the ball.
 * - Checking for collisions with walls and holes.
 * - Drawing the ball on the game board.
 * - Applying physics such as reflection and attraction based on nearby tiles.
*/
public class Ball {

    public char colour; // The color of the ball, used to determine its sprite and behavior
    public float x; // X index of the tile on the board
    public float y; // Y index of the tile on the board

    public boolean spawned; // Flag to indicate if the ball has been spawned
    public boolean sunk; // Flag to indicate if the ball has sunk into a hole
    public float radius; // Radius of the ball (based on the ball image)
    public float spawnOffset; // Offset to center the ball on its spawn tile

    public float xVel; // X velocity of the ball
    public float yVel; // Y velocity of the ball

    private float size; // The current size of the ball
    private float default_size; // The constant size of the ball (used for resetting size)

    /**
     * Constructor for the Ball object.
     * @param colour The color of the ball, which determines its image and behavior.
    */
    public Ball(char colour) {
        this.colour = colour;
        spawned = false;
        sunk = false;
        radius = getDimensions(); // Set the radius based on the ball's image dimensions
        spawnOffset = 0.5f - (radius / App.CELLSIZE); // Offset to center the ball in the tile
        size = radius*2;
        default_size = radius*2;
    }

    /**
     * Retrieves the dimensions (radius) of the ball based on its sprite.
     * @return The radius of the ball.
     */
    public float getDimensions() {
        float radius = 0;
        try {
            // Load the ball image from resources based on the ball color
            InputStream input = getClass().getResourceAsStream("/inkball/ball" + colour + ".png");
            if (input != null) {
                BufferedImage ballImage = ImageIO.read(input);
                radius = ballImage.getWidth() / 2; // Set radius as half the width of the image
            } else {
                System.out.println("Error: Resource not found");
            }
        } catch (IOException e) {
            System.out.println("Error loading image");
            e.printStackTrace();
        }
        return radius;
    }

    /**
     * Spawns the ball at the specified coordinates.
     * Sets the initial position and random velocity for the ball.
     * @param x The X-coordinate of the spawn location.
     * @param y The Y-coordinate of the spawn location.
    */
    public void spawn(int x, int y) {
        this.x = x + spawnOffset;
        this.y = y + spawnOffset;

        // Set random initial velocities in both X and Y directions
        xVel = (App.random.nextInt(2) == 0) ? -2 : 2;
        yVel = (App.random.nextInt(2) == 0) ? -2 : 2;

        spawned = true;
    }

    /**
     * Checks for collisions with walls and flips velocity if necessary.
     * @param app The main App object.
     * @param xOffset Offset in X direction for collision detection.
     * @param yOffset Offset in Y direction for collision detection.
     * @param velToFlip The velocity to be flipped (either X or Y).
     * @param postFlipDir The desired direction after the flip (positive for right/down).
     * @return The updated velocity after collision handling.
    */
    public float wallCollisionCheck(App app, float xOffset, float yOffset, float velToFlip, int postFlipDir) { // postFlipDir has down and right as positive
        int xTileVal = (int) (x + xOffset/App.CELLSIZE);
        int yTileVal = (int) (y + yOffset/App.CELLSIZE);
        char tileType = app.board[yTileVal][xTileVal].type;

        char prevTileType = ' ';
        if (xTileVal - 1 >= 0) {
            prevTileType = app.board[yTileVal][xTileVal - 1].type;
        }

        // Check for collisions with walls and update ball color based on wall type
        if ((Character.isDigit(tileType) && (prevTileType != 'H' && prevTileType != 'B' && prevTileType != 'A')) || tileType == 'X') {
            if (Math.abs(velToFlip) * postFlipDir == velToFlip * -1) { // Flip the velocity if necessary
                velToFlip *= -1;
            }
            if (tileType != 'X') {
                colour = tileType; // Change ball color based on tile type
            }
        }

        // Handle accelerator tiles ('A') which change ball speed
        if (tileType == 'A') {
            char nextTile = app.board[yTileVal][xTileVal + 1].type;
            float accel = 0.2f; // Acceleration factor
            switch (nextTile) {
                case '1': yVel -= accel; break; // Accelerate upwards
                case '2': xVel += accel; break; // Accelerate right
                case '3': yVel += accel; break; // Accelerate downwards
                case '4': xVel -= accel; break; // Accelerate left
            }
        }
        return velToFlip;
    }

    /**
     * Checks if the ball has collided with a hole and handles sinking behavior.
     * @param app The main App object.
     * @param xBallCentre The X-coordinate of the ball's center.
     * @param yBallCentre The Y-coordinate of the ball's center.
    */
    public void holeCollsionCheck(App app, float xBallCentre, float yBallCentre) {
        // Check surrounding tiles to identify a hole
        int xPos = (int) x;
        int yPos = (int) y;
        Tile tile = app.board[yPos][xPos];
        // Adjust position if ball is near a hole
        if (tile.type == 'E') {
            yPos -= 1;
            tile = app.board[yPos][xPos];
        }
        if (Character.isDigit(tile.type)) {
            xPos -= 1;
            tile = app.board[yPos][xPos];
        }

        // The ball is near a hole
        if (tile.type == 'H') {
            int xHolePos = tile.x + 1;
            int yHolePos = tile.y + 1;

            // Calculate the distance from the ball to the hole
            float xHoleVector = xHolePos - xBallCentre;
            float yHoleVector = yHolePos - yBallCentre;
            double distanceToHole = Math.sqrt(Math.pow(xHoleVector, 2) + Math.pow(yHoleVector, 2));

            // Handle scoring if the ball is close enough to the hole
            if (distanceToHole <= 0.3f) {
                // Ball sinks into the correct hole, increase score
                if (app.board[yPos][xPos + 1].type == colour || app.board[yPos][xPos + 1].type == '0' || colour == '0') {
                    System.out.println("score up");
                    app.currScore += app.scoreInc[(int) (colour - '0')];
                    boolean thisBallFound = false;
                    for (int i = 0; i < app.balls.length; i++) { // Get position of this ball, shift all one over, then add to end
                        if (app.balls[i] == this) {
                            thisBallFound = true;
                        }
                        if (thisBallFound && i + 1 < app.balls.length) {
                            app.balls[i] = app.balls[i+1];
                        }
                    }
                    app.balls[app.balls.length - 1  - app.rightHoleCount] = this;
                    app.rightHoleCount += 1;
                    sunk = true;
                }
                // Ball sinks into the wrong hole, decrease score
                else {
                    System.out.println("score down");
                    if (app.currScore - app.scoreDec[(int) (colour - '0')] < 0) {
                        app.currScore = 0;
                    }
                    else {
                        app.currScore -= app.scoreDec[(int) (colour - '0')];
                    }
                    boolean thisBallFound = false;
                    for (int i = 0; i < app.balls.length; i++) { // Get position of this ball, shift all one over, then add to end
                        if (app.balls[i] == this) {
                            thisBallFound = true;
                        }
                        if (thisBallFound && i + 1 < app.balls.length) {
                            app.balls[i] = app.balls[i+1];
                        }
                    }
                    app.balls[app.balls.length - 1  - app.rightHoleCount] = this;
                    spawned = false;
                    app.wrongHoleCount += 1;
                }
            }
            // Ball is near the hole, apply attraction force
            else if (distanceToHole <= 1) {
                xVel += xHoleVector * 0.5f;
                yVel += yHoleVector * 0.5f;
                if (distanceToHole <= 0.6f) {
                    xVel += xHoleVector;
                    yVel += yHoleVector;
                }
                size = (float) distanceToHole * default_size;
            }
        }
        // Reset ball size if not near a hole
        else {
            size = default_size;
        }
    }

    /**
     * Draws the ball on the game board and updates its position and velocity.
     * @param app The main App object.
    */
    public void draw(App app) {
        // If the ball hasn't been spawned yet, spawn it at a random spawner location
        if (!spawned) {
            int spawner = App.random.nextInt(app.spawnerCount);
            int[] spawnLoc = app.spawnLoc[spawner];
            spawn(spawnLoc[0], spawnLoc[1]);
        }

        // Draw the ball sprite
        PImage ball = app.getSprite("ball" + colour);
        app.image(ball, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, size, size);

        // If the game is not paused, update ball's position based on its velocity
        if (!app.paused) {
            x += xVel / App.CELLSIZE;
            y += yVel / App.CELLSIZE;
        }

        // Check for collisions with walls and adjust velocity accordingly
        yVel = wallCollisionCheck(app, radius, 0, yVel, 1); // Top check
        yVel = wallCollisionCheck(app, radius, radius*2, yVel, -1); // Bottom Check
        xVel = wallCollisionCheck(app, 0, radius, xVel, 1); // Right Check
        xVel = wallCollisionCheck(app, radius*2, radius, xVel, -1); // Left Check
        
        // Check if near hole and apply required logic
        holeCollsionCheck(app, x + spawnOffset, y + spawnOffset);

        // Check if ball is close to line, and reflect bounce based on normal vector if touching
        PVector[] ballClose;
        for (Line l : app.lines) {
            ballClose = l.isNearLine(x * App.CELLSIZE + radius, y * App.CELLSIZE + App.TOPBAR + radius, (int) radius + 5); // x + xOffset/App.CELLSIZE
            if (ballClose != null) {
                // Get normal vector of close line segment
                PVector lineDir = PVector.sub(ballClose[1], ballClose[0]);
                PVector normal = new PVector(-lineDir.y, lineDir.x);
                normal.normalize();

                // Acquire new velocity after bounce
                PVector velocity = new PVector(xVel, yVel);
                float dotProduct = PVector.dot(velocity, normal);
                PVector reflection = PVector.sub(velocity, PVector.mult(normal, 2 * dotProduct));

                xVel = reflection.x;
                yVel = reflection.y;

                // Remove the line
                app.lines.remove(l);
                break;
            }
        }
    }
    
}
