package uk.me.westmacott;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static uk.me.westmacott.Constants.*;
import static uk.me.westmacott.Constants.UNSET;

class MountainsLettuceLightningSpace {


    void render(ColourSeries colours,
                Availabilities availabilities,
                Masker masker,
                Seeder seeder,
                int[][] canvas,
                ImageSpitter spitter) throws IOException {

        final int imageWidth = Data.width(canvas);
        final int imageHeight = Data.height(canvas);
        final int debugTime = 10_000;

        System.out.println("Masking...");
        masker.mask(canvas);

        System.out.println("Seeding...");
        seeder.seed(availabilities, canvas);

        final int[] allColours = colours.asIntArray();
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
                                    + ", Elapsed: " + Data.formatTime(elapsedTime)
                                    + ", Remaining: " + Data.formatTime(remainingTime)
                                    + " or " + ((PIXEL_COUNT - i) / progressThisChunk) + " Minutes");
                    spitter.spitImage(canvas, "snapshot");
                }
            }

            if (!availabilities.live()) {
                System.out.println("Availabilities exhausted!");
                break;
            }

            int thisColour = allColours[i];
            Point bestPoint = availabilities.removeBest(thisColour);
            averages[bestPoint.x][bestPoint.y] = UNSET;
            canvas[bestPoint.x][bestPoint.y] = thisColour;

            for (Point neighbour : neighbours(bestPoint, imageWidth, imageHeight)) {
                if (canvas[neighbour.x][neighbour.y] == UNSET) {
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
        System.out.println("All colours rendered.");

        spitter.spitImage(canvas, "final");
        System.out.println("Final image rendered.");
    }

    private static int averageColour(List<Point> neighbours, int[][] canvas) {
        int red = 0, grn = 0, blu = 0, count = 0;
        for (Point neighbour : neighbours) {
            int rgb = canvas[neighbour.x][neighbour.y];
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

}
