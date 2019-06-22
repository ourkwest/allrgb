package uk.me.westmacott;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static uk.me.westmacott.Constants.*;
import static uk.me.westmacott.Constants.UNSET;

class MountainsLettuceLightningSpace {


    void render(ColourSeries colours, Availabilities availabilities, Canvas canvas, ImageSpitter spitter) throws IOException {

        final int imageWidth = canvas.getWidth();
        final int imageHeight = canvas.getHeight();
        final int debugTime = 10_000;

        final int[] allColours = colours.asIntArray();
        final int[][] data = Data.newArray(imageWidth, imageHeight);
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
                    new ImageSpitter().spitImage(canvas, "snapshot");
                }
            }

            int thisColour = allColours[i];
            Point bestPoint = availabilities.removeBest(thisColour);
            averages[bestPoint.x][bestPoint.y] = UNSET;

//            data[bestPoint.x][bestPoint.y] = thisColour;
            canvas.setPixel(bestPoint, thisColour);

            for (Point neighbour : neighbours(bestPoint, imageWidth, imageHeight)) {
                if (canvas.getPixel(neighbour) == UNSET) {
                    int currentAverage = averages[neighbour.x][neighbour.y];
                    if (currentAverage != UNSET) {
                        availabilities.remove(currentAverage, neighbour);
                    }
                    int newAverage = averageColour(neighbours(neighbour, imageWidth, imageHeight), canvas);
                    availabilities.add(newAverage, neighbour);
                    averages[neighbour.x][neighbour.y] = newAverage;
                }
            }
        }

        spitter.spitImage(canvas, "final");
        System.out.println("Final image rendered.");
    }

    private static int averageColour(List<Point> neighbours, Canvas canvas) {
        int red = 0, grn = 0, blu = 0, count = 0;
        for (Point neighbour : neighbours) {
            int rgb = canvas.getPixel(neighbour);
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

    private static Point[] neighbours = new Point[]{
            new Point(-1, -1), new Point( 0, -1), new Point( 1, -1),
            new Point(-1,  0),                    new Point( 1,  0),
            new Point(-1,  1), new Point( 0,  1), new Point( 1,  1)};

    private static java.util.List<Point> neighbours(Point input, int imageWidth, int imageHeight) {
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

    // TODO: move this
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
