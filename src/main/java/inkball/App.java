package inkball;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * The main application class for the Inkball game.
 * 
 * This class initializes the game environment, manages the game loop, 
 * handles user input, and coordinates interactions between game elements 
 * such as tiles, balls, and lines. It serves as the primary interface 
 * between the game logic and the visual representation.
 * 
 * Responsibilities of the App class include:
 * - Setting up the game window and rendering graphics.
 * - Managing game states (e.g., paused, running).
 * - Responding to user input for controlling the game.
 * - Updating the game state and drawing game objects each frame.
 * 
 * The App class extends PApplet from the Processing library, 
 * allowing for easy rendering and handling of graphics and animation.
*/
public class App extends PApplet {

    // Constants for cell size, board dimensions, top bar size, and window size
    public static final int CELLSIZE = 32;
    public static final int BOARD_WIDTH = 18;
    public static final int BOARD_HEIGHT = 18;
    public static final int TOPBAR = 2 * CELLSIZE;
    public static int WIDTH = CELLSIZE * BOARD_WIDTH; // 576
    public static int HEIGHT = CELLSIZE * BOARD_HEIGHT + TOPBAR; // 640

    // Game configuration constants
    public static final int FPS = 30;
    public static String[] COLOURS = {"grey", "orange", "blue", "green", "yellow"};
    public static int TEXT_SIZE = 20;

    // Game state variables related to board setup, configuration, and level handling
    private String configPath; // Path to configuration file
    private JSONObject config; // JSON object to hold config data
    private String[] levelLayout; // Array to hold the level layout
    public int levelToload; // Current level to load
    public int levelCount; // Total number of levels

    // Game objects and mechanics
    public Tile[][] board; // 2D array representing the game board with tiles
    public Ball[] balls; // Array of balls used in the game
    public int setBallCount; // Count of pre-set balls from the level file
    public int[][] spawnLoc; // Spawner locations for new balls
    public int extraBallCount; // Count of extra balls added during gameplay
    public int spawnerCount; // Number of spawners in the current level
    public int rightHoleCount; // Count of balls that have sunk into the correct hole
    public int wrongHoleCount; // Count of balls that have sunk into the wrong hole

    // Timer-related variables for handling game timing and spawning intervals
    public int time; // Total time for the level
    private int spawnInterval; // Time interval between ball spawns
    public float elapsedTime; // Time elapsed during the game
    private int frameCounter; // Frame counter for general timing
    private float elapsedBallTime; // Time elapsed since last ball spawn
    private int ballSpawnFrameCounter; // Frame counter for ball spawning
    public boolean levelOver; // Indicates if the level is over
    private int gameOverFrameCounter; // Frame counter for game-over animations
    public boolean paused; // Game pause status

    // Score-related variables
    public int currScore; // Current score of the player
    private int prevScore; // Previous score (carried over between levels)
    private float scoreIncMod; // Multiplier for score increase
    private float scoreDecMod; // Multiplier for score decrease
    public int[] scoreInc; // Array storing score increments based on ball color
    public int[] scoreDec; // Array storing score decrements based on ball color

    // Variables for handling user-drawn lines
    public ArrayList<Line> lines; // List of all drawn lines
    public Line line; // Current line being drawn
    public float prevX; // Previous X position of the mouse while drawing
    private float prevY; // Previous Y position of the mouse while drawing

    // Other general game state variables
    public HashMap<String, PImage> sprites = new HashMap<>(); // HashMap to store preloaded images for performance
    public static Random random = new Random(); // Random object for randomness in the game
    public int prevBallsToSpawn; // Previous count of balls to spawn
    private int movementFrameCounter; // Counter for ball movement frames
    private boolean ctrlPressed; // Flag to track if the control key is pressed

    // Constructor to initialise config file path
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Set up the window size based on constants.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load and cache sprites to avoid reloading them every frame.
     * 
     * @param s The name of the sprite to load.
     * @return The loaded PImage.
     */
    public PImage getSprite(String s) {
        PImage result = sprites.get(s); // Check if the image is already loaded
        if (result == null) {
            try {
                // Decode and load the image from the path
                result = loadImage(URLDecoder.decode(this.getClass().getResource(s +".png").getPath(), StandardCharsets.UTF_8.name()));
                sprites.put(s, result); // Store the image for potential future use
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * Setup game environment and variables when the program starts.
     */
	@Override
    public void setup() {
        frameRate(FPS); // Set the frame rate

        board = new Tile[BOARD_HEIGHT][BOARD_WIDTH];
        levelLayout = new String[BOARD_HEIGHT];
        levelToload = 1; // Level to start from
        levelCount = 3; // Total number of levels

        currScore = 0;
        prevScore = 0;
        scoreInc = new int[COLOURS.length];
        scoreDec = new int[COLOURS.length];

        config = loadJSONObject(configPath); // Load the configuration file
        loadLevel(levelToload); // Load the first level

        textSize(TEXT_SIZE); // Set the text size
        fill(0); // Set the fill color to black for text
        noStroke(); // Disable outline stroke

        // Initialise timers and counters
        frameCounter = 0;
        ballSpawnFrameCounter = 0;
        elapsedBallTime = 0;
        gameOverFrameCounter = 0;
        levelOver = false;

        prevBallsToSpawn = 0;
        movementFrameCounter = 0;
        rightHoleCount = 0;
        wrongHoleCount = 0;

        // Initialise line variables
        lines = new ArrayList<>();
        line = null;
        ctrlPressed = false;
    }

    /**
     * Load the level based on the provided level number.
     * 
     * @param levelNum The level number to load.
    */
    public void loadLevel(int levelNum) {
        extraBallCount = 0; // Reset extra ball count
        spawnerCount = 0; // Reset spawner count
        
        // Load the levels array from the config JSON
        JSONArray levels = config.getJSONArray("levels");
        JSONObject level  = levels.getJSONObject(levelNum - 1); // Get the specific level to load

        // Load the level layout from the file
        String levelFile = level.getString("layout");    
        levelLayout = loadStrings(levelFile); // Loads the text listed out by column

        // Update tiles on the board based on the level layout
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = new Tile(levelLayout[i].charAt(j), j, i); // Initialize each tile
                if (levelLayout[i].charAt(j) == 'B') {
                    extraBallCount++; // Count pre-set balls in the level
                }
                else if (levelLayout[i].charAt(j) == 'S') {
                    spawnerCount++; // Count spawners in the level
                }
            }
        }

        // Create balls to be spawned in and account for tile-specified balls
        JSONArray ballsJson = level.getJSONArray("balls");
        setBallCount = ballsJson.size();
        balls = new Ball[extraBallCount + setBallCount]; // Initialize ball array

        // Assign colors to the balls
        for (int i = 0; i < ballsJson.size(); i++) {
            String ballColour = ballsJson.getString(i);
            char ballType = '0';
            for (int j = 0; j < COLOURS.length; j++) {
                if (ballColour.equals(COLOURS[j])) {
                    ballType = (char) (j + '0'); // Assign color type based on the index
                }
            }
            balls[i + extraBallCount] = new Ball(ballType); // Add ball to the array
        }
        extraBallCount = 0; // Reset, as extra ball count will be recounted when spawning default balls

        // Save locations of spawners on the board
        spawnLoc = new int[spawnerCount][2]; // Array to hold spawner coordinates
        int spawnersFilled = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (levelLayout[i].charAt(j) == 'S') {
                    spawnLoc[spawnersFilled][0] = j;
                    spawnLoc[spawnersFilled][1] = i;
                    spawnersFilled++;
                }
            }
        }

        // Set the time limit for the level
        try {
            time = level.getInt("time");
            if (time < 0) {
                time = -1; // -1 is flag for infinite time
            }
        } catch (Exception e) {
            time = -1;
        }        
        
        spawnInterval = level.getInt("spawn_interval");
        elapsedTime = 0;

        // Set score modifiers based on level configuration
        scoreIncMod = level.getFloat("score_increase_from_hole_capture_modifier");
        scoreDecMod = level.getFloat("score_decrease_from_wrong_hole_modifier");
        JSONObject scoreIncJson = config.getJSONObject("score_increase_from_hole_capture");        
        for (int i = 0; i < scoreInc.length; i++) {
            scoreInc[i] = (int) (scoreIncJson.getInt(COLOURS[i]) * scoreIncMod); // Set score increment values
        }
        JSONObject scoreDecJson = config.getJSONObject("score_increase_from_hole_capture");        
        for (int i = 0; i < scoreDec.length; i++) {
            scoreDec[i] = (int) (scoreDecJson.getInt(COLOURS[i]) * scoreDecMod); // Set score decrement values
        }
    }

    /**
     * Handles key press events.
     * 
     * @param event the KeyEvent that triggered this method
    */
	@Override
    public void keyPressed(KeyEvent event){
        if (keyCode == CONTROL) {
            ctrlPressed = true;
        }
    }

    /**
     * Handles key release events.
     * 
     * @param event the KeyEvent that triggered this method
    */
	@Override
    public void keyReleased(KeyEvent event){
        if (!levelOver) {
            if (event.getKey() == 'r' || event.getKey() == 'R') {
                reset();
            }
            else if (event.getKey() == ' ') {
                paused = !paused;
            }
        }
        else if (levelToload == levelCount) { // allows resetting before all points added (should it be changed?)
            if (event.getKey() == 'r' || event.getKey() == 'R') {
                levelToload = 1;
                prevScore = 0;
                reset();
            }
        }
        if (keyCode == CONTROL) {
            ctrlPressed = false;
        }
    }

    /**
     * Handles mouse press events.
     * 
     * @param e the MouseEvent that triggered this method
    */
    @Override
    public void mousePressed(MouseEvent e) {
        if (!paused && !levelOver) {
            // Right-click to remove a line
            if (mouseButton == RIGHT || (mouseButton == LEFT && ctrlPressed)) {
                PVector[] mouseClose;
                for (Line l : lines) {
                    mouseClose = l.isNearLine(mouseX, mouseY, 5);
                    if (mouseClose != null) {
                        lines.remove(l);
                        break;
                    }
                }
            }
            // Left-click to add line
            else if (mouseButton == LEFT) {
                prevX = mouseX;
                prevY = mouseY;
                line = new Line();
                lines.add(line);
            }
        }        
    }
    
    /**
     * Handles mouse dragged events.
     * 
     * @param e the MouseEvent that triggered this method
    */
	@Override
    public void mouseDragged(MouseEvent e) {
        // add line segments to player-drawn line object if left mouse button is held
		
		// remove player-drawn line object if right mouse button is held 
		// and mouse position collides with the line
        if (!paused && !levelOver) {
            if (mouseButton == LEFT && !ctrlPressed) {
                line.addSegment(prevX, prevY, mouseX, mouseY);
                prevX = mouseX;
                prevY = mouseY; 
            }   
        }
    }

    /**
     * Handles mouse release events.
     * 
     * @param e the MouseEvent that triggered this method
    */
    @Override
    public void mouseReleased(MouseEvent e) {
        line = null; // for checking its been set
    }

    /**
     * Handles the display of time-related messages in the game, including the countdown timer,
     * pause status, and messages for when the time is up.
     *
     * <p>This method checks the game's current time settings and updates the displayed messages 
     * accordingly. It considers whether the game is paused and if the time has run out. 
     * The method also formats the positioning of the messages on the screen based on the specified 
     * horizontal and vertical gaps.</p>
     *
     * @param time         The remaining time for the level. A value of -1 indicates infinite time.
     * @param elapsedTime  The amount of time that has passed since the start of the level.
     * @param levelOver    A boolean indicating whether the level is over.
     * @param message      A string to hold the message to be displayed. This will be updated 
     *                     based on the game's current time state.
     * @param textHoriGap  The horizontal gap used for positioning the text.
     * @param textVertGap  The vertical gap used for positioning the text.
    */
    public void handleTimeMessages(int time, float elapsedTime, boolean levelOver, int textHoriGap, int textVertGap) {
        // If time is set (not infinite)
        String message;
        if (time != -1) {
            // If time up
            if (time - elapsedTime <= 0 && !levelOver) {
                message = "=== TIME'S UP ===";
                text(message, (int) ((WIDTH*0.5) - (textWidth(message)*0.5) + TEXT_SIZE), (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
                paused = true;
            }
            // If paused
            else if (paused) {
                message = "*** PAUSED ***";
                text(message, (int) ((WIDTH*0.5) - (textWidth(message)*0.5) + TEXT_SIZE), (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
            }
            message = "Time: ";
            text(message, WIDTH - (textHoriGap + textWidth(message)), (int) (textVertGap*1.5) + TEXT_SIZE); // consider if text is drawn from bottom for score and time
            text((int) (time - elapsedTime), WIDTH - textHoriGap, (int) (textVertGap*1.5) + TEXT_SIZE);
        }
        // If infinite time can still pause
        else if (paused) {
            message = "*** PAUSED ***";
            text(message, (int) ((WIDTH*0.5) - (textWidth(message)*0.5) + TEXT_SIZE), (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
        }
    }
    
    /**
     * Handles the behavior when a level is over, including score management, animation of tiles,
     * and transitioning to the next level if applicable.
     *
     * <p>This method updates the game's state when the current level has ended. It manages 
     * score increments based on time elapsed, animates the yellow tiles at the edges of the 
     * game board, and checks if the game has reached its conclusion.</p>
     *
     * @param levelOver A boolean indicating whether the level is over. If true, 
     *                  the method will execute the relevant actions for the end of the level.
    */
    public void levelOver(boolean levelOver){
        String message;
        if (levelOver) {
            paused = false;
            // Every 0.067 seconds will move yellow tiles and increase score
            if (gameOverFrameCounter % 2 == 0 && (time != -1 && time - elapsedTime > 0)) {
                currScore += 1;
                frameCounter += FPS;
            }

            // Move the two yellow tiles around edges
            if (gameOverFrameCounter % (18 * 2) < BOARD_WIDTH) {
                image(getSprite("wall4"), gameOverFrameCounter%BOARD_WIDTH * App.CELLSIZE, 0 * App.CELLSIZE + App.TOPBAR);
                image(getSprite("wall4"), ((BOARD_WIDTH - 1) - gameOverFrameCounter%BOARD_WIDTH) * App.CELLSIZE, (BOARD_HEIGHT - 1) * App.CELLSIZE + App.TOPBAR);
            }
            else if (gameOverFrameCounter % (18 * 2) < BOARD_WIDTH*2) {
                image(getSprite("wall4"), (BOARD_WIDTH - 1) * App.CELLSIZE, gameOverFrameCounter%BOARD_HEIGHT * App.CELLSIZE + App.TOPBAR);
                image(getSprite("wall4"), 0 * App.CELLSIZE, ((BOARD_HEIGHT - 1) - gameOverFrameCounter%BOARD_HEIGHT) * App.CELLSIZE + App.TOPBAR);
            }

            // Increase score until time runs out
            if (time - elapsedTime <= 0) {
                if (levelToload == levelCount) {
                    message = "=== ENDED ===";
                    text(message, (int) ((WIDTH*0.5) - (textWidth(message)*0.5) + TEXT_SIZE), (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
                }
                else {
                    prevScore = currScore;
                    levelToload++;
                    reset();
                }
            }
            gameOverFrameCounter++;
        }
    }

    /**
     * Main draw loop that gets called repeatedly by Processing.
     * Handles game state updates, rendering, and user interactions.
    */
	@Override
    public void draw() {
        background(200, 200, 200); // Clear background

        // text placement
        int textHoriGap = 60;
        int textVertGap = 25;
        String message;

        // Drawing score
        message = "Score: ";
        text(message, WIDTH - (textHoriGap + textWidth(message)), textVertGap);
        text(currScore, WIDTH - textHoriGap, textVertGap);

        // Drawing time
        if (!paused && !levelOver) {
            frameCounter++;
        }
        elapsedTime = (float) frameCounter / FPS;
        elapsedBallTime = (float) ballSpawnFrameCounter / FPS;

        handleTimeMessages(time, elapsedTime, levelOver, textHoriGap, textVertGap);

        // Drawing tiles
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Tile t = board[i][j];                
                if (t.draw(this)) {
                    j++;
                }
            }
        }        

        // Drawing active balls
        int ballsToSpawn = (int) elapsedBallTime / spawnInterval + extraBallCount;

        for (int i = 0; i < ballsToSpawn - rightHoleCount - wrongHoleCount; i++) { // drawing all balls
            Ball b = balls[i];
            b.draw(this); 
        }

        // Drawing upcoming balls
        int ballGap = 6;
        int ballDiameter = (int) balls[0].radius * 2;
        int rectX = 10;
        rect(rectX , CELLSIZE / 2, ballGap*6 + ballDiameter*5, ballGap*2 + ballDiameter); // x, y, w, h
        
        // Allowing slide animation (1px/f) when a ball spawns
        movementFrameCounter = (prevBallsToSpawn != ballsToSpawn) ? movementFrameCounter + 1 : 0; // note that smallest spawn interval is 2 seconds due to the slide
        int movementCalc = 0;
        if (prevBallsToSpawn < ballsToSpawn) {
            movementCalc = ballDiameter + ballGap - movementFrameCounter; // moves ball 1px/f to new pos using this offset from new pos
        }
        if (movementCalc == 0) {
            prevBallsToSpawn = ballsToSpawn;
        }

        // Draw the upcoming balls
        int ballsToShow = 0;
        int ballsLeft = 0;
        for (int i = 0; i < balls.length; i++) {
            if (!balls[i].spawned) {
                if (ballsToShow < 5) {
                    image(getSprite("ball" + balls[i].colour), ((ballDiameter + ballGap)*ballsToShow) + movementCalc + rectX + ballGap, (CELLSIZE / 2) + ballGap);
                    ballsToShow++;
                }
                ballsLeft++;
            }
        }
        // Increase time until next ball if any remain
        if (ballsLeft > 0 && !paused){
            ballSpawnFrameCounter++;
        }
        
        // Draw timer until next ball
        fill(200, 200, 200);
        rect(rectX + ballGap*6 + ballDiameter*5, CELLSIZE / 2, ballGap*2 + ballDiameter, ballGap*2 + ballDiameter); // covers new ball to slide in from right
        fill(0);
        if (ballsLeft != 0) {
            String timeToBall = String.format("%.1f", spawnInterval - (elapsedBallTime % spawnInterval));
            text(timeToBall, rectX*2 + ballGap*6 + ballDiameter*5, (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
        }

        // Draw lines
        for (Line l : lines) {
            l.draw(this);
        }

        // Check if level is over
        levelOver = true;
        for (int i = 0; i < balls.length; i++) { // shows balls in queue
            if (!balls[i].sunk) {
                levelOver = false;
                break;
            }
        }

        // If level over
        levelOver(levelOver);
    }

    /**
     * Reset the game state to prepare for the next level or a replay.
    */
    public void reset() {
        // Reset timers
        frameCounter = 0;
        ballSpawnFrameCounter = 0;
        elapsedBallTime = 0;
        gameOverFrameCounter = 0;

        // Reset balls
        prevBallsToSpawn = 0;
        movementFrameCounter = 0;
        rightHoleCount = 0;
        wrongHoleCount = 0;

        // Reset game status
        paused = false;
        levelOver = false;

        // Reset score
        currScore = prevScore;

        // Reset lines
        lines = new ArrayList<Line>();
        line = null;

        // Reload the specified level layout and reset the score
        loadLevel(levelToload);    
    }

    /**
     * Entry point to the application.
     * @param args Arguments provided at runtime (if any).
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }

}