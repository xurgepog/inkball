package inkball;

import processing.core.PImage;

/**
 * Represents a tile on the game board in the Inkball game.
 * 
 * A Tile can represent different game elements such as walls, holes, 
 * entry points, and balls. Each tile is identified by a type, 
 * which determines its behavior and appearance in the game.
 * 
 * Responsibilities of the Tile class include:
 * - Storing the type and position of the tile.
 * - Drawing the tile on the game board.
 * - Handling specific behaviors based on the tile type, such as 
 *   spawning balls or defining entry points.
*/
public class Tile {
    // Tile properties
    public char type; // Type of the tile, represented by a character
    public int x; // X index of the tile on the board
    public int y; // Y index of the tile on the board

    private char HOLE_FLAG = 'E'; // Special flag for tiles that are part of a hole
    private boolean spawned = false; // Flag to track whether a ball has been spawned on this tile

    /**
        * Constructor for the Tile object.
        * @param type The character representing the type of the tile ('B', 'S', etc.).
        * @param x The x-coordinate of the tile on the board.
        * @param y The y-coordinate of the tile on the board.
    */
    public Tile(char type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    /**
        * Draw the tile on the board.
        * @param app The PApplet object used for drawing.
        * @return boolean Whether the tile needs to be skipped (e.g., if it spans multiple cells).
    */
    public boolean draw(App app) {
        // Load the default tile sprite
        PImage tile = app.getSprite("tile");
        boolean skipNext = false; // Flag to determine if the next tile should not be checked

        // If the tile type is a digit load the corresponding wall sprite
        if (Character.isDigit(type)) {
            tile = app.getSprite("wall" + type);
        }
        // Switch on the tile type to determine specific behaviors
        switch (type) {
            case 'X':
                tile = app.getSprite("wall0");               
                break;
            case 'S':
                tile = app.getSprite("entrypoint");
                break;
            case 'H':
                tile = app.getSprite("hole" + app.board[y][x+1].type);
                skipNext = true;
                app.board[y+1][x].type = HOLE_FLAG; // won't draw these tiles
                app.board[y+1][x+1].type = HOLE_FLAG;
                break;
            case 'B':
                if (!spawned) {
                    app.balls[app.extraBallCount] = new Ball(app.board[y][x+1].type);
                    app.balls[app.extraBallCount].spawn(x, y);
                    app.extraBallCount++;
                }
                skipNext = true;
                break;
            case 'A':
                tile = app.getSprite("acel" + app.board[y][x+1].type);
                skipNext = true;
        }
        // Draw the skipped tile as blank
        if (skipNext) {
            app.image(app.getSprite("tile"), (x+1) * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR); // draws tile for next then skips it to avoid number being read as wall type
        }

         // If the tile isn't a part of a hole, draw it at its position
        if (type != 'E') {
            app.image(tile, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR);
        }
        spawned = true;

        return skipNext;
    }
    
}