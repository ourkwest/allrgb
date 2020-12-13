package uk.me.westmacott;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static uk.me.westmacott.Constants.*;

public class MountainsLettuceLightningSpace {

    public static void main(String[] args) {
        System.out.println(80 % 90);
        System.out.println(90 % 90);
        System.out.println(100 % 90);
        System.out.println(-10 % 90);
    }

//    public static void render (int[][] canvas,
//                               Iterable<Integer> colours,
//                               AvailablePointsByTargetColour availablePointsByTargetColour,
//                               ImageSpitter spitter) throws IOException {
//        render(canvas, colours, availablePointsByTargetColour, spitter, false, Echo.NoopEcho());
//    }

    public static void render(int[][] canvas,
                              Iterable<Integer> colours,
                              AvailablePointsByTargetColour availablePointsByTargetColour,
                              ImageSpitter spitter,
                              boolean wrap,
                              Echo echo) throws IOException {

        final int imageWidth = Data.width(canvas);
        final int imageHeight = Data.height(canvas);

        final int[] allColours = StreamSupport.stream(colours.spliterator(), false).mapToInt(x -> x).toArray();
        final int[][] targets = Data.newArray(imageWidth, imageHeight);
        final int totalColours = allColours.length;

        final Function<Point, List<Point>> neighbourFinder = neighbourFinderFor(imageWidth, imageHeight, wrap);

        long start = System.currentTimeMillis();

        System.out.println("Iterating...");
        System.out.println(new String(new char[totalColours / DEBUG_EVERY]).replace("\0", "_"));
        for (int i = 0; i < totalColours; i++) {

            if (i % DEBUG_EVERY == 0) {
                System.out.print("#");
                if (i % SNAPSHOT_EVERY == 0) {
                    spitter.spitImage(canvas, "snapshot");
                }
            }

            if (availablePointsByTargetColour.empty()) {
                System.out.println();
                System.out.println("AvailablePointsByTargetColour exhausted! (i = " + i + ")");
                break;
            }

            int thisColour = allColours[i];
            Point bestPoint = availablePointsByTargetColour.removeClosest(thisColour);
            targets[bestPoint.x][bestPoint.y] = UNSET;
            canvas[bestPoint.x][bestPoint.y] = thisColour;
            echo.echo(canvas, bestPoint);

            for (Point neighbour : neighbourFinder.apply(bestPoint)) {
                if (canvas[neighbour.x][neighbour.y] == UNSET) {
                    int currentAverage = targets[neighbour.x][neighbour.y];
                    if (currentAverage != UNSET) {
                        availablePointsByTargetColour.remove(currentAverage, neighbour);
                    }
                    int newAverage = averageColour(neighbourFinder.apply(neighbour), canvas);
                    availablePointsByTargetColour.add(newAverage, neighbour);
                    targets[neighbour.x][neighbour.y] = newAverage;
                }
            }
        }
        System.out.println();
        System.out.println("All colours rendered.");
        System.out.println("Iteration time: " + (System.currentTimeMillis() - start) / 1000 + " seconds");

        String finalImageName = spitter.spitImage(canvas, "final");
        System.out.println("Final image rendered: " + finalImageName);
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

    private static Function<Point, List<Point>> neighbourFinderFor(int imageWidth, int imageHeight, boolean wrap) {
        if (wrap) {
            return input -> {
                List<Point> output = new LinkedList<>();
                for (Point neighbour : neighbours) {
                    int x = Math.floorMod(input.x + neighbour.x, imageWidth);
                    int y = Math.floorMod(input.y + neighbour.y, imageHeight);
                    output.add(new Point(x, y));
                }
                return output;
            };
        }
        return input -> {
            List<Point> output = new LinkedList<>();
            for (Point neighbour : neighbours) {
                int x = input.x + neighbour.x;
                int y = input.y + neighbour.y;
                if (0 <= x && x < imageWidth && 0 <= y && y < imageHeight) {
                    output.add(new Point(x, y));
                }
            }
            return output;
        };
    }

}
