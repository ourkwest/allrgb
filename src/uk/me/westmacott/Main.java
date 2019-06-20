package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static uk.me.westmacott.Constants.*;

/**
 * Mountains and lettuce leaves and lightning and space.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        final int imageWidth = IMAGE_SIZE * 2;
        final int imageHeight = IMAGE_SIZE;
        final int debugTime = 10_000;

        final int[][] data = Data.newArray(imageWidth, imageHeight);
        final int[] allColours = ColourSeries.allColoursShuffled();
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
                int x = (imageWidth / 2) + (int)(2000 * Math.sin(theta));
                int y = (imageHeight / 2) + (int)(2000 * Math.cos(theta));
                int colour = Color.getHSBColor((float) (theta / TAU), 1.0f, 1.0f).getRGB() & 0xFFFFFF;
                availabilities.add(colour, new Point(x, y));
            }
        }


        final int[][] averages = Data.newArray(imageWidth, imageHeight);


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
                if (data[x][y] != UNSET) {
                    image.setRGB(x, y, data[x][y] | 0xFF000000);
                }
            }
        }
        String filename = String.format("Render_%03d-%s", NUMBER, suffix);
        ImageIO.write(image, "png", new File("./" + filename + ".png"));
    }

    private static Point[] neighbours = new Point[]{
            new Point(-1, -1), new Point( 0, -1), new Point( 1, -1),
            new Point(-1,  0),                    new Point( 1,  0),
            new Point(-1,  1), new Point( 0,  1), new Point( 1,  1)};

    private static List<Point> neighbours(Point input, int imageWidth, int imageHeight) {
        List<Point> output = new LinkedList<>();
        for (Point neighbour : neighbours) {
            int x = input.x + neighbour.x;
            int y = input.y + neighbour.y;
            if (0 <= x && x < imageWidth && 0 <= y && y < imageHeight) {
                output.add(new Point(x, y));
            }
        }
        return output;
    }

    static String display(long millis) {
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
