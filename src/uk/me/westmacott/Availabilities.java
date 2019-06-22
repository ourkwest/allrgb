package uk.me.westmacott;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * For each colour, stores a list of available pixels that would like to be as close to that colour as possible.
 */
public class Availabilities {

    private RgbOctree colours = new RgbOctree();
    private HashMap<Integer, LinkedList<Point>> pointsByColour = new HashMap<>();
    private int pointCount = 0;

    @Override
    public String toString() {
        return "Availabilities(" + colours.size() + " : " + pointCount + ")";
    }

    boolean live() {
        return colours.size() > 0;
    }

    void add(int colour, Point point) {
        colours.add(colour);
        pointsByColour.computeIfAbsent(colour, x -> new LinkedList<>()).addFirst(point); // TODO: experiment: addLast could make a big difference to algorithm!!!
        pointCount++;
    }

    Point removeBest(int targetColour) {
        int bestColour = colours.probablyNearTo2(targetColour);
        LinkedList<Point> pointsForBestColour = pointsByColour.get(bestColour);
        Point bestPoint = pointsForBestColour.removeLast();
        pointCount--;
        if (pointsForBestColour.isEmpty()) {
            colours.remove(bestColour);
            pointsByColour.remove(bestColour);
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
