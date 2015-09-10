package uk.me.westmacott;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RgbOctreeTest {

    static Random random = new Random();
    static long timeA = 0;
    static long timeB = 0;
    static long matches = 0;
    static long fails = 0;

    static double totalError = 0;
    static double minError = Integer.MAX_VALUE;
    static double maxError = Integer.MIN_VALUE;

    public static void main(String[] args) {

//        System.out.println("Distance: " + distance(0, 0, 0, 3, 4, 0));


//        Color color = new Color(255, 0, 0);
//        int rgb = color.getRGB() & 0xFFFFFF;

//        tree.add(new Color(255, 0, 0).getRGB());
//        tree.add(new Color(255, 1, 0).getRGB());

        RgbOctree tree = new RgbOctree();
        colours = new LinkedList<>();

        Set<Integer> doubleCheck = new HashSet<>();

        for (int i = 0; i < 10000; i++) {
            int x = generateColour();
            tree.add(x);
            add(x);
            doubleCheck.add(x);
        }
        System.out.println("Initial sizes: " + colours.size() + " / " + tree.size());

        for (int j = 0; j < 10000; j++) {

            for (int i = 0; i < 10; i++) {
                int aColour = colours.get(0);
                remove(aColour);
                tree.remove(aColour);
                doubleCheck.remove(aColour);

                if (colours.size() != tree.size()) {
                    System.out.println("Sizes: " + colours.size() + " / " + tree.size());
                }

                int x = generateColour();
                tree.add(x);
                add(x);
                doubleCheck.add(x);

                if (colours.size() != tree.size()) {
                    System.out.println("Sizes: " + colours.size() + " / " + tree.size());
                }
            }

            int target = generateColour();

            long a = System.currentTimeMillis();
            int result1 = tree.probablyNearTo(target);
            long b = System.currentTimeMillis();
            int result2 = nearestTo(target);
            long c = System.currentTimeMillis();

            timeA += (b - a);
            timeB += (c - b);

            if (result1 == result2) {
                matches++;
            }
            else {

                double actualDistance = distance(result1, target);
                double expectedDistance = distance(result2, target);
                double difference = actualDistance - expectedDistance;

                if (difference < 0) {
                    System.out.println();
                    System.out.println("DIFF!!!");
                    System.out.println("target  = " + new Color(target));
                    System.out.println("result1 = " + new Color(result1));
                    System.out.println("result2 = " + new Color(result2));
                    System.out.println("dist1   = " + actualDistance);
                    System.out.println("dist2   = " + expectedDistance);
                    System.out.println("diff    = " + difference);
                    System.out.println(colours.contains(result1));
                    System.out.println(doubleCheck.contains(result1));
                }

                if (difference == 0) {
                    matches++;
                }
                else {
                    totalError += difference;
                    minError = Math.min(minError, difference);
                    maxError = Math.max(maxError, difference);
                    fails++;
                }
            }

            System.out.print("\r" + j);
        }

        System.out.println();
        System.out.println("Success: " + ((matches * 100.0) / (matches + fails)) + "% (" + matches + " : " + fails + ")");
        System.out.println("Octree: " + Main.display(timeA));
        System.out.println("TryAll: " + Main.display(timeB));

        System.out.println("Average Error: " + (totalError / fails));
        System.out.println("Max / Min : " + maxError + " / " + minError);

    }

    private static int generateColour() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b).getRGB() & 0xFFFFFF;
    }


    static List<Integer> colours;
    static void add(int colour) {
        colours.add(colour);
    }
    static void remove(int colour) {
        colours.remove(colours.indexOf(colour));
    }
    static int nearestTo(int colour) {
        int best = 0;
        double leastDistance = Integer.MAX_VALUE;
        for (int candidate : colours) {
            double distance = distance(colour, candidate);
            if (distance < leastDistance) {
                leastDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    public static double distance(int rgb1, int rgb2) {
        int red1 = (rgb1 >> 16) & 0xFF;
        int green1 = (rgb1 >> 8) & 0xFF;
        int blue1 = rgb1 & 0xFF;
        int red2 = (rgb2 >> 16) & 0xFF;
        int green2 = (rgb2 >> 8) & 0xFF;
        int blue2 = rgb2 & 0xFF;
        return distance(red1, green1, blue1, red2, green2, blue2);
    }

    public static double distance(int red1, int green1, int blue1, int red2, int green2, int blue2) {
        int dr = red1 - red2;
        int dg = green1 - green2;
        int db = blue1 - blue2;
        return Math.pow(dr * dr + dg * dg + db * db, 0.5);
    }



}
