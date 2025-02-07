package inkball;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * Represents a line that can be drawn in the Inkball game.
 * 
 * Lines can interact with the balls, affecting their movement and behavior. 
 * They may be used as obstacles or pathways in the game. The class 
 * includes methods for determining if a ball is near a line and for 
 * handling interactions between the ball and the line.
 * 
 * Responsibilities of the Line class include:
 * - Storing the coordinates and properties of the line.
 * - Checking for proximity to balls and calculating collision responses.
 * - Managing the visual representation of the line on the game board.
*/
public class Line {
    // List of line segments making up the entire line
    public ArrayList<PVector[]> lines = new ArrayList<>(); // Holds start and end vectors of each line

    /**
        * Add a segment to the line.
        * @param x1 The starting x-coordinate of the line segment.
        * @param y1 The starting y-coordinate of the line segment.
        * @param x2 The ending x-coordinate of the line segment.
        * @param y2 The ending y-coordinate of the line segment.
    */
    void addSegment(float startX, float startY, float endX, float endY) {
        PVector start = new PVector(startX, startY);
        PVector end = new PVector(endX, endY);
        PVector[] line = {start, end};
        lines.add(line);
    }

    /**
        * Draw the entire line on the screen.
        * @param app The PApplet object used for drawing.
    */
    void draw(App app) {
        app.stroke(0); // Set stroke color to black for the line
        app.strokeWeight(10);
        for (PVector[] points : lines) {
            app.line(points[0].x, points[0].y, points[1].x, points[1].y); // Draw each segment
        }
        app.noStroke(); // Disable stroke after drawing the line
    }

    /**
        * Check if the mouse is near a line segment.
        * @param mouseX The current mouse x-coordinate.
        * @param mouseY The current mouse y-coordinate.
        * @param tolerance The distance threshold for proximity detection.
        * @return PVector[] The segment near the mouse, or null if no segment is near.
    */
    PVector[] isNearLine(float mouseX, float mouseY, int threshold) {
        PVector mousePos = new PVector(mouseX, mouseY);
        // Check each line segment to see if the mouse is within a certain threshold
        for (PVector[] l : lines) {
            // will determine closest point using projection
            PVector lineVector = PVector.sub(l[1], l[0]); // B - A -> AB
            PVector pointVector = PVector.sub(mousePos, l[0]);
            
            float projScalar = PVector.dot(pointVector, lineVector) / PVector.dot(lineVector, lineVector);
            projScalar = PApplet.constrain(projScalar, 0, 1); // to ensure closest point is within line

            PVector closestPoint = PVector.add(l[0], PVector.mult(lineVector, projScalar));
            float distance = PVector.dist(closestPoint, mousePos);
            if (distance <= threshold) {
                return l; // Return the segment if within tolerance
            }
        }
        return null; // No segment found near the mouse
    }
}
