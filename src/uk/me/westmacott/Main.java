package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Mountains and lettuce leaves and lightning and space.
public class Main {

    static final int COLOUR_CUBE_SIZE = 256;
    static final int IMAGE_SIZE = (int) Math.pow(COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE, 0.5);
    static final int PIXEL_COUNT = COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE;
    static final int UNSET = -1;

    static final int NUMBER = Data.readAndWrite("Number", () -> 0, i -> i++);
    public static final double TAU = 2.0 * Math.PI;

    public static void main(String[] args) throws IOException {


        final int imageWidth = IMAGE_SIZE;
        final int imageHeight = IMAGE_SIZE * 2;
        final int debugTime = 10_000;


        final int[][] data = Data.newArray(UNSET, imageWidth, imageHeight);
        final int[] allColours = Data.readOrWrite("allColoursInterleaved", Main::getAllColoursInterleaved);
        final Availabilities availabilities = new Availabilities();


        System.out.println("Seeding image...");
//        availabilities.add(Color.GRAY.getRGB() & 0xFFFFFF, new Point(IMAGE_SIZE / 2, IMAGE_SIZE / 2));

//        {
//            List<Integer> list = IntStream.range(0, imageWidth).mapToObj(i -> i).collect(Collectors.toList());
//            Collections.shuffle(list);
//            list.stream().forEach(x -> availabilities.add(Color.BLACK.getRGB() & 0xFFFFFF, new Point(x, 0)));
//        }

        {
            for (double theta = 0; theta < TAU; theta += 0.1) {
                int x = (imageWidth / 2) + (int)(100 * Math.sin(theta));
                int y = (imageHeight / 2) + (int)(100 * Math.cos(theta)) + 500;
                int colour = Color.getHSBColor((float) (theta / TAU), 1.0f, 1.0f).getRGB() & 0xFFFFFF;
                availabilities.add(colour, new Point(x, y));
            }
        }


        final int[][] averages = Data.newArray(UNSET, imageWidth, imageHeight);


        long start = System.currentTimeMillis();
        long mark = start;
        long remainingTime;
        long elapsedTime;
        int lastI = 0;
        int progressThisChunk;


        System.out.println("Iterating...");
        for (int i = 0; i < PIXEL_COUNT; i++) {

            if (i%1000 == 0) {
                long now = System.currentTimeMillis();
                if (now > mark + debugTime) {
                    mark += debugTime;

                    progressThisChunk = i - lastI;
                    lastI = i;

                    elapsedTime = now - start;
                    remainingTime = (elapsedTime * PIXEL_COUNT / i) - elapsedTime;

                    System.out.println(
                            (Math.round((10000.0 * i) / PIXEL_COUNT) / 100) + "%"
                                    + ", " + availabilities
                                    + ", Elapsed: " + display(elapsedTime)
                                    + ", Remaining: " + display(remainingTime)
                                    + " or " + ((PIXEL_COUNT - i) / progressThisChunk) + " Minutes");
                    spitImage(data, "snapshot");
                }
            }

            int thisColour = allColours[i];
            Point bestPoint = availabilities.removeBest(thisColour);
            averages[bestPoint.x][bestPoint.y] = UNSET;

            data[bestPoint.x][bestPoint.y] = thisColour;

            for (Point neighbour : neighbours(bestPoint, imageWidth, imageHeight)) {
                if (data[neighbour.x][neighbour.y] == UNSET) {
                    int currentAverage = averages[neighbour.x][neighbour.y];
                    if (currentAverage != UNSET) {
                        availabilities.remove(currentAverage, neighbour);
                    }
                    int newAverage = averageColour(neighbours(neighbour, imageWidth, imageHeight), data);
                    availabilities.add(newAverage, neighbour);
                    averages[neighbour.x][neighbour.y] = newAverage;
                }
            }
        }

        spitImage(data, "final");
        System.out.println("Final image rendered.");
    }

    private static int[] getAllColours() {
        System.out.println("Generating colours...");
        List<Integer> allColoursAsIntegers = IntStream.range(0, PIXEL_COUNT).mapToObj(i -> i).collect(Collectors.toList());

        System.out.println("Arranging colours...");
        Collections.shuffle(allColoursAsIntegers);
        Collections.sort(allColoursAsIntegers, (c1, c2) -> getHue(c1) - getHue(c2));

        System.out.println("Unboxing colours...");
        return allColoursAsIntegers.stream().mapToInt(i -> i).toArray();
    }

    private static int[] getAllColoursInterleaved() {
        int[] primitiveArray = getAllColours();
        System.out.println("Interleaving colours...");
        int[] interleaved = new int[PIXEL_COUNT];
        int i = 0;
        int j = PIXEL_COUNT - 1;
        int k = 0;
        while (k < (PIXEL_COUNT - 3)) {
            interleaved[k++] = primitiveArray[i++];
            interleaved[k++] = primitiveArray[j--];
        }
        return interleaved;
    }

    private static int averageColour(List<Point> neighbours, int[][] data) {
        int red = 0, grn = 0, blu = 0, count = 0;
        for (Point neighbour : neighbours) {
            int rgb = data[neighbour.x][neighbour.y];
            if (rgb != UNSET) {
                red += (rgb >> 16) & 0xFF;
                grn += (rgb >> 8) & 0xFF;
                blu += rgb & 0xFF;
                count++;
            }
        }
        red /= count;
        grn /= count;
        blu /= count;
        return (red << 16) | (grn << 8) | blu;
    }

    private static void spitImage(int[][] data, Object suffix) throws IOException {
        int width = data.length;
        int height = data[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // TODO: is there a bulk operation for this?
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, data[x][y] | 0xFF000000);
            }
        }
        String filename = String.format("Render_%03d-%s", NUMBER, suffix);
        ImageIO.write(image, "png", new File("./" + filename + ".png"));
    }

    public static Point[] neighbours = new Point[]{
            new Point(UNSET, UNSET), new Point( 0, UNSET), new Point( 1, UNSET),
            new Point(UNSET,  0),                    new Point( 1,  0),
            new Point(UNSET,  1), new Point( 0,  1), new Point( 1,  1)};

    public static List<Point> neighbours(Point input, int imageWidth, int imageHeight) {
        List<Point> output = new LinkedList<>();
        for (int i = 0; i < neighbours.length; i++) {
            int x = input.x + neighbours[i].x;
            int y = input.y + neighbours[i].y;
            if (0 <= x && x < imageWidth && 0 <= y && y < imageHeight) {
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
        double duration = millis;
        String unit = "Millisecond";
        if (duration >= 1000) {
            duration /= 1000;
            unit = "Second";
            if (duration >= 60) {
                duration /= 60;
                unit = "Minute";
                if (duration >= 60) {
                    duration /= 60;
                    unit = "Hour";
                }
            }
        }
        if (duration > 1) {
            unit += "s";
        }
        return String.format("%4.2f %s", duration, unit);
    }
}
