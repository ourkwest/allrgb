package uk.me.westmacott;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

public class Availabilities {

    RgbOctree colours = new RgbOctree();
    HashMap<Integer, LinkedList<Point>> pointsByColour = new HashMap<>();

    int pointCount = 0;

    @Override
    public String toString() {
        return "Availabilities(" + colours.size() + " : " + pointCount + ")";
    }

    public void add(int colour, Point point) {
        colours.add(colour);
        pointsByColour.computeIfAbsent(colour, x -> new LinkedList()).addFirst(point);
        pointCount++;
    }

    public Point removeBest(int target) {
        int bestColour = colours.probablyNearTo2(target);
        LinkedList<Point> pointsForBestColour = pointsByColour.get(bestColour);
        Point bestPoint = pointsForBestColour.removeLast();
        pointCount--;
        if (pointsForBestColour.isEmpty()) {
            colours.remove(bestColour);
            pointsByColour.remove(bestColour);
        }
        return bestPoint;
    }

    public void remove(int colour, Point point) {
        LinkedList<Point> pointsForColour = pointsByColour.get(colour);
        pointsForColour.remove(point);
        pointCount--;
        if (pointsForColour.isEmpty()) {
            colours.remove(colour);
            pointsByColour.remove(colour);
        }
    }

}
