package uk.me.westmacott;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * For each colour, stores a list of available pixels that would like to be as close to that colour as possible.
 */
public class AvailablePointsByTargetColour {

    private RgbOctree colours = new RgbOctree();
    private HashMap<Integer, LinkedList<Point>> pointsByColour = new HashMap<>();
    private int pointCount = 0;

    @Override
    public String toString() {
        return "AvailablePointsByTargetColour(" + colours.size() + " : " + pointCount + ")";
    }

    boolean empty() {
        return colours.size() == 0;
    }

    public void add(Color colour, int x, int y) {
        add(colour.getRGB() & 0xFFFFFF, x, y);
    }

    public void add(int colour, int x, int y) {
        add(colour, new Point(x, y));
    }

    public void add(int colour, Point point) {
        colours.add(colour);
        pointsByColour.computeIfAbsent(colour, x -> new LinkedList<>()).addFirst(point);
        pointCount++;
    }

    Point removeClosest(int targetColour) {
        int closestColour = colours.probablyNearTo2(targetColour);
        LinkedList<Point> pointsForClosestColour = pointsByColour.get(closestColour);
        Point bestPoint = pointsForClosestColour.removeLast(); // TODO: experiment: removeFirst could make a big difference to algorithm!!!
        pointCount--;
        if (pointsForClosestColour.isEmpty()) {
            colours.remove(closestColour);
            pointsByColour.remove(closestColour);
        }
        return bestPoint;
    }

    void remove(int colour, Point point) {
        LinkedList<Point> pointsForColour = pointsByColour.get(colour);
        pointsForColour.remove(point);
        pointCount--;
        if (pointsForColour.isEmpty()) {
            colours.remove(colour);
            pointsByColour.remove(colour);
        }
    }

}
