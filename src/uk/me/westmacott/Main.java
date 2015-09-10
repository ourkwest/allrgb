package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    static final int COLOUR_CUBE_SIZE = 256;
    static final int IMAGE_SIZE = (int) Math.pow(COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE, 0.5);
    static final int PIXEL_COUNT = COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE;
    public static final int UNSET = -1;

    public static void main(String[] args) throws IOException {


        // 1. Create a randomly sorted list of all the colours.
        // 2. Starting from a random seed, add the colours one by one in the 'best' place.

        System.out.println("Generating colours...");
        List<Integer> allColoursAsIntegers = IntStream.range(0, PIXEL_COUNT).mapToObj(i -> i).collect(Collectors.toList());

        System.out.println("Arranging colours...");
        Collections.shuffle(allColoursAsIntegers);
        Collections.sort(allColoursAsIntegers, (c1, c2) -> getHue(c1) - getHue(c2));

        System.out.println("Unboxing colours...");
        final int[] allColours = allColoursAsIntegers.stream().mapToInt(i -> i).toArray();

        final int[][] data = new int[IMAGE_SIZE][IMAGE_SIZE];
        for (int i = 0; i < IMAGE_SIZE; i++) {
            for (int j = 0; j < IMAGE_SIZE; j++) {
                data[i][j] = UNSET;
            }
        }
        final int[][] averages = new int[IMAGE_SIZE][IMAGE_SIZE];


        System.out.println(COLOUR_CUBE_SIZE);
        System.out.println(PIXEL_COUNT);
        System.out.println(IMAGE_SIZE);

        System.out.println(new Color(allColours[0]));
        System.out.println(new Color(allColours[1]));
        System.out.println(new Color(allColours[2]));


        Set<Point> available = new HashSet<>();
        available.add(new Point(IMAGE_SIZE / 2, IMAGE_SIZE / 2));


        // for each sorted colour - find the best place to put it.

        int snapshotNumber = 0;
        long start = System.currentTimeMillis();
        long mark = start;
        long remainingTime;
        long elapsedTime;

        for (int i = 0; i < PIXEL_COUNT; i++) {

            if (i%1000 == 0) {
                long now = System.currentTimeMillis();
                if (now > mark + 30_000) {
                    mark += 30_000;

                    elapsedTime = now - start;
                    remainingTime = (elapsedTime * PIXEL_COUNT / i) - elapsedTime;
                    snapshotNumber%=10;

                    System.out.print("\r" + 0 + "..." + i + "..." + PIXEL_COUNT
                            + ", List: " + available.size()
                            + ", Elapsed: " + display(elapsedTime)
                            + ", Remaining: " + display(remainingTime)
                            + ", Snapshot: " + snapshotNumber);
                    spitImage(data, snapshotNumber);
                    snapshotNumber++;
                }
            }

            int thisColour = allColours[i];

            Point best = null;

            int leastDifference = Integer.MAX_VALUE;


            for (Point candidate : available) { // could parallelise this?
                int average = averages[candidate.x][candidate.y];
                int difference = difference(thisColour, average);
//                System.out.println("Retrieved " + candidate + " with average " + new Color(average) + " and difference " + difference + " from " + new Color(thisColour));
                if (difference < leastDifference) {
                    leastDifference = difference;
                    best = candidate;
                }
            }

//            if (i%2 == 0) {
//                int leastDifference = Integer.MAX_VALUE;
//                for (Point candidate : available) {
//                    int difference = 0;
//                    int neighbourCount = 0;
//                    for (Point neighbour : neighbours(candidate)) {
//                        int neighbourColour = data[neighbour.x][neighbour.y];
//                        if (neighbourColour != UNSET) {
//                            difference += difference(thisColour, neighbourColour);
//                            neighbourCount++;
//                        }
//                    }
//                    if (neighbourCount > 0) {
//                        difference /= neighbourCount;
//                    }
//                    if (difference < leastDifference) {
//                        leastDifference = difference;
//                        best = candidate;
//                    }
//                }
//            } else {
//                int leastDifference = Integer.MAX_VALUE;
//                for (Point candidate : available) {
//                    for (Point neighbour : neighbours(candidate)) {
//                        int neighbourColour = data[neighbour.x][neighbour.y];
//                        if (neighbourColour != UNSET) {
//                            int difference = difference(thisColour, neighbourColour);
//                            if (difference < leastDifference) {
//                                leastDifference = difference;
//                                best = candidate;
//                            }
//                        }
//                    }
//                }
//            }

            if (best != null) {
                data[best.x][best.y] = thisColour;
                available.remove(best);
                for (Point neighbour : neighbours(best)) {
                    if (data[neighbour.x][neighbour.y] == UNSET) {
                        available.add(neighbour);
                        averages[neighbour.x][neighbour.y] = averageColour(neighbours(neighbour), data);
//                        System.out.println("Adding " + neighbour + " with average " + new Color(average));
                    }
                }
            }
        }

        spitImage(data, "final");
        
        // 17000 - 25 hours

    }



    private static int averageColour(List<Point> neighbours, int[][] data) {
//        System.out.println("Calculating average...");
        int red = 0, grn = 0, blu = 0, count = 0;
        for (Point neighbour : neighbours) {
            int rgb = data[neighbour.x][neighbour.y];
            if (rgb != UNSET) {
//            System.out.println(" > " + neighbour + " : " + new Color(rgb));
                red += (rgb >> 16) & 0xFF;
                grn += (rgb >> 8) & 0xFF;
                blu += rgb & 0xFF;
                count++;
            }
        }
        red /= count;
        grn /= count;
        blu /= count;
        //        System.out.println("Average: " + i + " : " + new Color(i));
        return (red << 16) | (grn << 8) | blu;
    }

    private static void spitImage(int[][] data, Object snapshotNumber) throws IOException {
        BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < IMAGE_SIZE; x++) {
            for (int y = 0; y < IMAGE_SIZE; y++) {
                image.setRGB(x, y, data[x][y]);
            }
        }
        String filename = "snapshot-" + snapshotNumber;
        ImageIO.write(image, "png", new File("./" + filename + ".png"));
    }

    public static int MAX_DIFFERENCE = difference(Color.BLACK.getRGB(), Color.WHITE.getRGB());

    public static int difference(int rgb1, int rgb2) {
        int red1 = (rgb1 >> 16) & 0xFF;
        int green1 = (rgb1 >> 8) & 0xFF;
        int blue1 = rgb1 & 0xFF;
        int red2 = (rgb2 >> 16) & 0xFF;
        int green2 = (rgb2 >> 8) & 0xFF;
        int blue2 = rgb2 & 0xFF;
        return difference(red1, green1, blue1, red2, green2, blue2);
    }

    public static int difference(int red1, int green1, int blue1, int red2, int green2, int blue2) {
        int dr = red1 - red2;
        int dg = green1 - green2;
        int db = blue1 - blue2;
        return dr * dr + dg * dg + db * db;
    }

    public static Point[] neighbours = new Point[]{
            new Point(UNSET, UNSET), new Point( 0, UNSET), new Point( 1, UNSET),
            new Point(UNSET,  0),                    new Point( 1,  0),
            new Point(UNSET,  1), new Point( 0,  1), new Point( 1,  1)};

    public static List<Point> neighbours(Point input) {
        List<Point> output = new LinkedList<>();
        for (int i = 0; i < neighbours.length; i++) {
            int x = input.x + neighbours[i].x;
            int y = input.y + neighbours[i].y;
            if (0 < x && x < IMAGE_SIZE && 0 < y && y < IMAGE_SIZE) {
                output.add(new Point(x, y));
            }
        }
        return output;
    }

    public static int getHue(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return getHue(red, green, blue);
    }

    public static int getHue(int red, int green, int blue) {

        float min = Math.min(Math.min(red, green), blue);
        float max = Math.max(Math.max(red, green), blue);

        float hue;
        if (max == red) {
            hue = (green - blue) / (max - min);

        } else if (max == green) {
            hue = 2f + (blue - red) / (max - min);

        } else {
            hue = 4f + (red - green) / (max - min);
        }

        hue = hue * 60;
        if (hue < 0) hue = hue + 360;

        return Math.round(hue);
    }

    public static String display(long millis) {
        long duration = millis;
        String unit = " Millisecond";
        if (duration >= 1000) {
            duration /= 1000;
            unit = " Second";
            if (duration >= 60) {
                duration /= 60;
                unit = " Minute";
                if (duration >= 60) {
                    duration /= 60;
                    unit = " Hour";
                }
            }
        }
        if (duration > 1) {
            unit += "s";
        }
        return duration + unit;
    }
}
