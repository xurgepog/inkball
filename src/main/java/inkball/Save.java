// package inkball;

// import java.io.UnsupportedEncodingException;
// import java.net.URLDecoder;
// import java.nio.charset.StandardCharsets;
// import java.util.HashMap;
// import java.util.Random;

// import processing.core.PApplet;
// import processing.core.PImage;
// import processing.data.JSONArray;
// import processing.data.JSONObject;
// import processing.event.KeyEvent;
// import processing.event.MouseEvent;

// // import minesweeper.Tile;

// public class App extends PApplet {

//     // constants
//     public static final int CELLSIZE = 32;
//     public static final int BOARD_WIDTH = 18;
//     public static final int BOARD_HEIGHT = 18;
//     public static final int TOPBAR = 2 * CELLSIZE;
//     public static int WIDTH = CELLSIZE * BOARD_WIDTH; // 576
//     public static int HEIGHT = CELLSIZE * BOARD_HEIGHT + TOPBAR; // 640
    
//     public static final int INITIAL_PARACHUTES = 1; // what is this for

//     public static final int FPS = 30;

//     public static String[] COLOURS = {"grey", "orange", "blue", "green", "yellow"};
//     public static int TEXT_SIZE = 20;

//     // board set up
//     public String configPath;

//     JSONObject config;
//     String[] levelLayout;
//     public int levelToload;

//     public Tile[][] board;
//     public Ball[] balls;
//     public int setBallCount;
//     public int[][] spawnLoc;
//     public int extraBallCount;
//     public int spawnerCount;
//     public int rightHoleCount;
//     public int wrongHoleCount;

//     // timer
//     public int time;
//     public int spawnInterval;
//     public float elapsedTime;
//     public int frameCounter;
//     public boolean paused;

//     // score
//     public int currScore;
//     public int prevScore;
//     public float scoreIncMod;
//     public float scoreDecMod;
//     public int[] scoreInc;
//     public int[] scoreDec;

//     // lines drawing
//     public float prevX;
//     public float prevY;

//     // other
//     public static Random random = new Random();
//     public int prevBallsToSpawn;
//     public int movementFrameCounter;

//     public App() {
//         this.configPath = "config.json";
//     }

//     /**
//      * Initialise the setting of the window size.
//      */
// 	@Override
//     public void settings() {
//         size(WIDTH, HEIGHT);
//     }

//     private HashMap<String, PImage> sprites = new HashMap<>(); // store loaded images to reduce runtime

//     public PImage getSprite(String s) {
//         PImage result = sprites.get(s);
//         if (result == null) {
//             try {
//                 result = loadImage(URLDecoder.decode(this.getClass().getResource(s +".png").getPath(), StandardCharsets.UTF_8.name()));
//                 sprites.put(s, result);
//             } catch (UnsupportedEncodingException e) {
//                 throw new RuntimeException(e);
//             }
//         }
//         return result;
//     }


// 	@Override
//     public void setup() {
//         frameRate(FPS);
        
//         board = new Tile[BOARD_HEIGHT][BOARD_WIDTH];
//         levelLayout = new String[BOARD_HEIGHT];
//         levelToload = 2;

//         currScore = 0;
//         prevScore = 0;
//         scoreInc = new int[COLOURS.length];
//         scoreDec = new int[COLOURS.length];

// 		config = loadJSONObject(configPath);
//         loadLevel(levelToload);

//         textSize(TEXT_SIZE);
//         fill(0);
//         noStroke();

//         prevBallsToSpawn = 0;
//         movementFrameCounter = 0;
//         rightHoleCount = 0; // want one ball to spawn instantly
//         wrongHoleCount = 0;

//         prevX = mouseX;
//         prevY = mouseY;
//     }

//     public void loadLevel(int levelNum) {
//         extraBallCount = 0;
//         spawnerCount = 0;
        
//         // loading level array
//         JSONArray levels = config.getJSONArray("levels");
//         JSONObject level  = levels.getJSONObject(levelNum - 1); // will pass in 1 for level 1 for easier logic

//         String levelFile = level.getString("layout");    
//         levelLayout = loadStrings(levelFile); // loads the text listed out by column

//         // go through level layout and update tiles to match
//         for (int i = 0; i < board.length; i++) {
//             for (int j = 0; j < board[i].length; j++) {
//                 board[i][j] = new Tile(levelLayout[i].charAt(j), j, i);
//                 if (levelLayout[i].charAt(j) == 'B') {
//                     extraBallCount++;
//                 }
//                 else if (levelLayout[i].charAt(j) == 'S') {
//                     spawnerCount++;
//                 }
//             }
//         }

//         // create balls to be spawned in and account for tile specified balls
//         JSONArray ballsJson = level.getJSONArray("balls");
//         setBallCount = ballsJson.size();
//         balls = new Ball[extraBallCount + setBallCount];

//         for (int i = 0; i < ballsJson.size(); i++) {

//             String ballColour = ballsJson.getString(i);
//             char ballType = '0';
//             for (int j = 0; j < COLOURS.length; j++) {
//                 if (ballColour.equals(COLOURS[j])) {
//                     ballType = (char) (j + '0');
//                 }
//             }
//             balls[i + extraBallCount] = new Ball(ballType);
//         }
//         extraBallCount = 0; // will be recounted when spawning default balls

//         // save locations of spawners
//         spawnLoc = new int[spawnerCount][2];
//         int spawnersFilled = 0;
//         for (int i = 0; i < board.length; i++) {
//             for (int j = 0; j < board[i].length; j++) {
//                 if (levelLayout[i].charAt(j) == 'S') {
//                     spawnLoc[spawnersFilled][0] = j;
//                     spawnLoc[spawnersFilled][1] = i;
//                     spawnersFilled++;
//                 }
//             }
//         }

//         // set up time and score settings
//         try {
//             time = level.getInt("time");
//             if (time < 0) {
//                 time = -1; // -1 is flag for infinite time
//             }
//         } catch (Exception e) {
//             time = -1;
//         }        
        
//         spawnInterval = level.getInt("spawn_interval");
//         elapsedTime = 0;

//         scoreIncMod = level.getFloat("score_increase_from_hole_capture_modifier");
//         scoreDecMod = level.getFloat("score_decrease_from_wrong_hole_modifier");
//         JSONObject scoreIncJson = config.getJSONObject("score_increase_from_hole_capture");        
//         for (int i = 0; i < scoreInc.length; i++) {
//             scoreInc[i] = (int) (scoreIncJson.getInt(COLOURS[i]) * scoreIncMod);
//         }
//         JSONObject scoreDecJson = config.getJSONObject("score_increase_from_hole_capture");        
//         for (int i = 0; i < scoreDec.length; i++) {
//             scoreDec[i] = (int) (scoreDecJson.getInt(COLOURS[i]) * scoreDecMod);
//         }
//     }

//     /**
//      * Receive key pressed signal from the keyboard.
//      */
// 	@Override
//     public void keyPressed(KeyEvent event){
        
//     }

//     /**
//      * Receive key released signal from the keyboard.
//      */
// 	@Override
//     public void keyReleased(KeyEvent event){
//         if (event.getKey() == 'r' || event.getKey() == 'R') {
//             reset();
//         }
//         else if (event.getKey() == ' ') {
//             paused = !paused;
//         }
//     }

//     @Override
//     public void mousePressed(MouseEvent e) {
//         // create a new player-drawn line object
//         prevX = mouseX;
//         prevY = mouseY;
//     }
	
// 	@Override
//     public void mouseDragged(MouseEvent e) {
//         // add line segments to player-drawn line object if left mouse button is held
		
// 		// remove player-drawn line object if right mouse button is held 
// 		// and mouse position collides with the line
//         stroke(0);  // Set the line color
//         strokeWeight(5);  // Set line thickness
//         line(prevX, prevY, mouseX, mouseY);
//         noStroke();
//     }
//     // create line object that holds line segment objects - holds points with lines drawn between? will use the points and ball velocity to calculate bounce
//     // if angle changes by certain amount create new segment

//     @Override
//     public void mouseReleased(MouseEvent e) {
		
//     }

//     /**
//      * Draw all elements in the game by current frame.
//      */
// 	@Override
//     public void draw() {
//         background(200, 200, 200);

//         // text placement
//         int textHoriGap = 60;
//         int textVertGap = 25;
//         String message;

//         // drawing score
//         message = "Score: ";
//         text(message, WIDTH - (textHoriGap + textWidth(message)), textVertGap);
//         text(currScore, WIDTH - textHoriGap, textVertGap);

//         // drawing time
//         if (!paused) {
//             frameCounter++;
//         }
//         elapsedTime = (float) frameCounter / FPS;

//         if (time != -1) {
//             if (time - elapsedTime <= 0) {
//                 message = "=== TIME'S UP ===";
//                 text(message, (int) (WIDTH*0.5), (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
//                 paused = true;
//             }
//             else if (paused) {
//                 message = "*** PAUSED ***";
//                 text(message, (int) (WIDTH*0.5), (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
//             }
//             message = "Time: ";
//             text(message, WIDTH - (textHoriGap + textWidth(message)), (int) (textVertGap*1.5) + TEXT_SIZE); // consider if text is drawn from bottom for score and time
//             text((int) (time - elapsedTime), WIDTH - textHoriGap, (int) (textVertGap*1.5) + TEXT_SIZE);
//         }
//         else if (paused) {
//             message = "*** PAUSED ***";
//             text(message, (int) (WIDTH * 0.5), (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
//         }

//         // drawing tiles
//         for (int i = 0; i < board.length; i++) {
//             for (int j = 0; j < board[i].length; j++) {
//                 Tile t = board[i][j];                
//                 if (t.draw(this)) {
//                     j++;
//                 }
//             }
//         }        

//         // upcoming balls
//         int ballsToSpawn = (int) elapsedTime / spawnInterval + extraBallCount; // this is probs the issue - only increase when not all balls have been spawned, pause when all spawned

//         int ballGap = 6;
//         int ballDiameter = (int) balls[0].radius * 2;
//         int rectX = 10;
//         rect(rectX , CELLSIZE / 2, ballGap*6 + ballDiameter*5, ballGap*2 + ballDiameter); // x, y, w, h

//         movementFrameCounter = (prevBallsToSpawn != ballsToSpawn) ? movementFrameCounter + 1 : 0; // note that smallest spawn interval is 2 seconds due to the slide
//         int movementCalc = 0;
//         if (prevBallsToSpawn < ballsToSpawn) {
//             movementCalc = ballDiameter + ballGap - movementFrameCounter; // moves ball 1px/f to new pos using this offset from new pos
//         }
        

//         int ballsToShow = 0;
//         int ballsLeft = 0;
//         for (int i = 0; i < balls.length; i++) { // shows balls in queue
//             if (!balls[i].spawned) {
//                 if (ballsToShow < 5) {
//                     image(getSprite("ball" + balls[i].colour), ((ballDiameter + ballGap)*ballsToShow) + movementCalc + rectX + ballGap, (CELLSIZE / 2) + ballGap);
//                     ballsToShow++;
//                 }
//                 ballsLeft++;
//             }
//             else {
//             }
//         }
//         if (movementCalc == 0) {
//             prevBallsToSpawn = ballsToSpawn;
//         }

//         // drawing balls
//         for (int i = 0; i < ballsToSpawn + 1 - rightHoleCount - wrongHoleCount; i++) { // drawing all balls
//             Ball b = balls[i];
//             b.draw(this); 
//         }
        
//         fill(200, 200, 200); // default ball into wrong hole bad
//         rect(rectX + ballGap*6 + ballDiameter*5, CELLSIZE / 2, ballGap*2 + ballDiameter, ballGap*2 + ballDiameter); // covers new ball to slide in from right
//         fill(0);

//         if (ballsLeft != 0) { // shows timer of balls left to spawn
//             String timeToBall = String.format("%.1f", spawnInterval - (elapsedTime % spawnInterval));
//             text(timeToBall, rectX*2 + ballGap*6 + ballDiameter*5, (int) (TOPBAR*0.5 + TEXT_SIZE*0.5));
//         }
//     }

//     public void reset() { // change to respawn
//         frameCounter = 0;
//         currScore = prevScore;
//         paused = false;
//         loadLevel(levelToload);
//         rightHoleCount = 0;
//         wrongHoleCount = 0;
//     }

//     public static void main(String[] args) {
//         PApplet.main("inkball.App");
//     }

// }

// // consider encapsulation???
// // remove any unneccasary "global" variables
// // modularise draw stuff?

// // need to think about drawing lines first maybe? - not really described in document
// // fix bounds error in hole collision
// // make hole collision not repetitive
// // make it so that balls to spawn doesnt keep increasing when all balls are in play
// // multiple balls despawning at once or am i tripping?? - is real - seems to occur when no more balls in upcoming list
// // possibly make it so that if a ball leaves a hole it isnt slingshotted -> reduce speed using trig and 2 against 2 initial values

// // perhaps move spawner spawned balls thing into here to that spawned can be set to true instantly - believe this is what is causing the jitter