package uk.me.westmacott;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static uk.me.westmacott.Constants.*;
import static uk.me.westmacott.Constants.UNSET;

class MountainsLettuceLightningSpace {


    void render(ColourSeries colours,
                AvailablePointsByTargetColour availablePointsByTargetColour,
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
        seeder.seed(availablePointsByTargetColour, canvas);

        final int[] allColours = colours.asIntArray();
        final int[][] averages = Data.newArray(imageWidth, imageHeight);

        long start = System.currentTimeMillis();

        System.out.println("Iterating...");
        System.out.println(new String(new char[PIXEL_COUNT / DEBUG_COUNT]).replace("\0", "_"));
        for (int i = 0; i < PIXEL_COUNT; i++) {

            if (i%DEBUG_COUNT == 0) {
                System.out.print("#");
            }

            if (availablePointsByTargetColour.empty()) {
                System.out.println();
                System.out.println("AvailablePointsByTargetColour exhausted!");
                break;
            }

            int thisColour = allColours[i];
            Point bestPoint = availablePointsByTargetColour.removeClosest(thisColour);
            averages[bestPoint.x][bestPoint.y] = UNSET;
            canvas[bestPoint.x][bestPoint.y] = thisColour;

            for (Point neighbour : neighbours(bestPoint, imageWidth, imageHeight)) {
                if (canvas[neighbour.x][neighbour.y] == UNSET) {
                    int currentAverage = averages[neighbour.x][neighbour.y];
                    if (currentAverage != UNSET) {
                        availablePointsByTargetColour.remove(currentAverage, neighbour);
                    }
                    int newAverage = averageColour(neighbours(neighbour, imageWidth, imageHeight), canvas);
                    availablePointsByTargetColour.add(newAverage, neighbour);
                    averages[neighbour.x][neighbour.y] = newAverage;
                }
            }
        }
        System.out.println();
        System.out.println("All colours rendered.");
        System.out.println("Iteration time: " + (System.currentTimeMillis() - start) / 1000 + " seconds");

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

}
