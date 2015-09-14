package uk.me.westmacott;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RgbOctree {


    /*
    *
    *
    * 1234, 1234, 1234
    *
    * all nodes exist implicitly
    *
    *   map rgb to index-chain
    *
    *   index 0
    *       r & g & b -> index 1-8 -> index 9-24
    *
    * 0 -> 0
    * 1 -> 8^0 - 8^1
    * 2 -> 8^1 - 8^2
    *
    *
    *
    * descend recursively marking all that match as
    *
    *
    *
    * */

    int colourCount = 8 * 8 * 8 * 8 * 8 * 8 * 8 * 8;

    //    int[][] counts = new int[][]{null, null, null, null, null, null, null, null};
    static int DEPTH = 9;
    int[][] counts = new int[DEPTH][];
    {
        int size = 1;
        for (int depth = 0; depth < DEPTH; depth++) {
            counts[depth] = new int[size];
            size *= 8;
        }
//        System.out.println(colourCount);
    }

    public static final int UNSET = -1;
    int[] indexByRgb = new int[colourCount];
    int[] rgbByIndex = new int[colourCount];
    {
        Arrays.fill(indexByRgb, UNSET);
        Arrays.fill(rgbByIndex, UNSET);
    }



//    private static int getOctant(int r, int g, int b) {
//        int red = (0xFF0000 & colourA) > (0xFF0000 & colourB) ? 1 : 0;
//        int grn = (0x00FF00 & colourA) > (0x00FF00 & colourB) ? 2 : 0;
//        int blu = (0x0000FF & colourA) > (0x0000FF & colourB) ? 4 : 0;
//        return red + grn + blu;
//    }

    private static int[] decompose(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return new int[]{r, g, b};
    }


    int size() {
        return counts[0][0];
    }

    void add(int rgb) {
        if (indexByRgb[rgb] == UNSET) {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            int index = 0;
            for (int depth = 0; depth < DEPTH; depth++) {
                int mask = 0b100000000 >> depth;
                index = 8 * index
                        + ((r & mask) == 0 ? 0 : 4)
                        + ((g & mask) == 0 ? 0 : 2)
                        + ((b & mask) == 0 ? 0 : 1);
                counts[depth][index]++;
            }
            rgbByIndex[index] = rgb;
            indexByRgb[rgb] = index;
        }
    }

    int removeNear(int rgb) {
        // Can optimise?
        int nearish = probablyNearTo(rgb);
        remove(nearish);
        return nearish;
    }

    boolean contains(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int index = 0;
        for (int depth = 0; depth < DEPTH; depth++) {
            int mask = 0b100000000 >> depth;
            index = 8 * index
                    + ((r & mask) == 0 ? 0 : 4)
                    + ((g & mask) == 0 ? 0 : 2)
                    + ((b & mask) == 0 ? 0 : 1);
        }
        return counts[8][index] != 0;
    }

    void remove(int rgb) {
        if (indexByRgb[rgb] != UNSET) {
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            int index = 0;
            for (int depth = 0; depth < DEPTH; depth++) {
                int mask = 0b100000000 >> depth;
                index = 8 * index
                        + ((r & mask) == 0 ? 0 : 4)
                        + ((g & mask) == 0 ? 0 : 2)
                        + ((b & mask) == 0 ? 0 : 1);
                counts[depth][index]--;
            }
            rgbByIndex[index] = UNSET;
            indexByRgb[rgb] = UNSET;
        }
    }

    int probablyNearTo(int rgb) {

        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int goodIndex = 0, goodDepth = 0;

        int index = 0;
        for (int depth = 0; depth < 8; depth++) {
            int mask = 0b100000000 >> depth;
            index = 8 * index
                    + ((r & mask) == 0 ? 0 : 4)
                    + ((g & mask) == 0 ? 0 : 2)
                    + ((b & mask) == 0 ? 0 : 1);
            if (counts[depth][index] > 0) {
                goodIndex = index;
                goodDepth = depth;
            } else {
                break;
            }
        }

        int min = goodIndex;
        int max = goodIndex;
        for (int depth = goodDepth + 1; depth < DEPTH; depth++) {
            min = 8 * min;
            max = 8 * max + 7;
        }

        int best = 0;
        int leastDistance = Integer.MAX_VALUE;
        for (int i = min; i <= max; i++) {
            if (rgbByIndex[i] != UNSET) {
                int distance = distance(rgbByIndex[i], rgb);
                if (distance < leastDistance) {
                    leastDistance = distance;
                    best = rgbByIndex[i];
                }
            }
        }

        return best;
    }

    int nearestTo(int rgb) {

        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int goodIndex = 0, goodDepth = 0;

        int index = 0;
        for (int depth = 0; depth < 8; depth++) {
            int mask = 0b100000000 >> depth;
            index = 8 * index
                    + ((r & mask) == 0 ? 0 : 4)
                    + ((g & mask) == 0 ? 0 : 2)
                    + ((b & mask) == 0 ? 0 : 1);
            if (counts[depth][index] > 0) {
                goodIndex = index;
                goodDepth = depth;
            } else {
                break;
            }
        }

        int min = goodIndex;
        int max = goodIndex;
        for (int depth = goodDepth + 1; depth < DEPTH; depth++) {
            min = 8 * min;
            max = 8 * max + 7;
        }

        int best = 0;
        int leastDistance = Integer.MAX_VALUE;
        for (int i = min; i <= max; i++) {
            if (rgbByIndex[i] != UNSET) {
                int distance = distance(rgbByIndex[i], rgb);
                if (distance < leastDistance) {
                    leastDistance = distance;
                    best = rgbByIndex[i];
                }
            }
        }

//        HashSet<CacheEntry> cache = new HashSet<>(); // possibly slower with only 27 elements

        LinkedList<CacheEntry> cache = new LinkedList<>();
        cache.add(new CacheEntry(goodDepth, goodIndex, best, leastDistance));

        for (int r2 : new int[]{clampLow(r - leastDistance), r, clampHigh(r + leastDistance)}) {
            for (int g2 : new int[]{clampLow(g - leastDistance), g, clampHigh(g + leastDistance)}) {
                for (int b2 : new int[]{clampLow(b - leastDistance), b, clampHigh(b + leastDistance)}) {
                    probablyNearToCached(r, g, b, r2, g2, b2, cache);
                }
            }
        }

        for (CacheEntry cacheEntry : cache) {
            if (cacheEntry.distance < leastDistance) {
                leastDistance = cacheEntry.distance;
                best = cacheEntry.best;
            }
        }

        return best;

        // AND THEN:
        //
        // use least distance to generate 27? points and do it all again!
        // but don't recalculate things that have already been calculated


    }

    static class CacheEntry {
        final int depth;
        final int index;
        final int best;
        final int distance;
        CacheEntry(int depth, int index, int best, int distance) {
            this.depth = depth;
            this.index = index;
            this.best = best;
            this.distance = distance;
        }
    }

    int clampLow(int n) {
        return n > 0 ? n : 0;
    }

    int clampHigh(int n) {
        return n < 255 ? n : 255;
    }

    void probablyNearToCached(int r, int g, int b, int r2, int g2, int b2, LinkedList<CacheEntry> cache) {

        int goodIndex = 0, goodDepth = 0;

        int index = 0;
        for (int depth = 0; depth < 8; depth++) {
            int mask = 0b100000000 >> depth;
            index = 8 * index
                    + ((r2 & mask) == 0 ? 0 : 4)
                    + ((g2 & mask) == 0 ? 0 : 2)
                    + ((b2 & mask) == 0 ? 0 : 1);
            if (counts[depth][index] > 0) {
                goodIndex = index;
                goodDepth = depth;
            } else {
                break;
            }
        }

        for (CacheEntry entry : cache) {
            if (entry.index == goodIndex && entry.depth == goodDepth) {
                return;
            }
        }

        int min = goodIndex;
        int max = goodIndex;
        for (int depth = goodDepth + 1; depth < DEPTH; depth++) {
            min = 8 * min;
            max = 8 * max + 7;
        }

        int best = 0;
        int leastDistance = Integer.MAX_VALUE;
        for (int i = min; i <= max; i++) {
            if (rgbByIndex[i] != UNSET) {
                int distance = distance(rgbByIndex[i], r, g, b);
                if (distance < leastDistance) {
                    leastDistance = distance;
                    best = rgbByIndex[i];
                }
            }
        }

        cache.add(new CacheEntry(goodDepth, goodIndex, best, leastDistance));
    }


    public void debugToFile(String filename) throws IOException {
        BufferedImage image = new BufferedImage(256 * 3, 256, BufferedImage.TYPE_INT_RGB);

        for (int rgb : rgbByIndex) {
            if (rgb != UNSET) {

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                image.setRGB(g, b, rgb);
                image.setRGB(256 + r, b, rgb);
                image.setRGB(512 + r, g, rgb);
            }
        }

//        String filename = TODAY + "-snapshot-" + snapshotNumber;
        ImageIO.write(image, "png", new File("./" + filename + ".png"));
    }

    void showDebugPane() {
        new ImageFrame(
                1000,
                new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB),
                image -> {
                    for (int x = 0; x < 512; x++) {
                        for (int y = 0; y < 512; y++) {
                            image.setRGB(x, y, 0);
                        }
                    }
                    for (int rgb : rgbByIndex) {
                        if (rgb != UNSET) {
                            int r = (rgb >> 16) & 0xFF;
                            int g = (rgb >> 8) & 0xFF;
                            int b = rgb & 0xFF;

                            image.setRGB(g, b, rgb);        image.setRGB(256 + r, b, rgb);
                            image.setRGB(g, 256 + r, rgb);
                        }
                    }
                });
    }

//    public void debugToScreen(int i) throws IOException {
//
//        Graphics graphics = debugImage.getGraphics();
//        graphics.setColor(Color.GRAY);
//        graphics.fillRect(0, 0, 256 * 3, 256);
//
//        for (int rgb : rgbByIndex) {
//            if (rgb != UNSET) {
//                int r = (rgb >> 16) & 0xFF;
//                int g = (rgb >> 8) & 0xFF;
//                int b = rgb & 0xFF;
//
//                debugImage.setRGB(g, b, rgb);
//                debugImage.setRGB(256 + r, b, rgb);
//                debugImage.setRGB(512 + r, g, rgb);
//            }
//        }
//
//        graphics.setColor(Color.BLACK);
//        graphics.drawString("" + i, 10, 240);
//        graphics.dispose();
//
//        frame.repaint();
//    }


    static class ImageFrame extends JFrame {

        private BufferedImage image;
        private final Consumer<BufferedImage> renderFn;

        public ImageFrame(long refreshPeriodMillis, BufferedImage image, Consumer<BufferedImage> renderFn) {
            this.image = image;
            this.renderFn = renderFn;

            setTitle("ImageTest");
            getContentPane().setPreferredSize(new Dimension(image.getWidth(null), image.getHeight(null)));
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            pack();
            setVisible(true);

            Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(
                    this::repaint,
                    0,
                    refreshPeriodMillis,
                    TimeUnit.MILLISECONDS);
        }

        @Override
        public void paint(Graphics g) {
            int y = (int) (getSize().getHeight() - getContentPane().getHeight());
            renderFn.accept(image);
            g.drawImage(image, 0, y, this);
        }
    }


    public static int distance(int rgb1, int rgb2) {
        int red1 = (rgb1 >> 16) & 0xFF;
        int green1 = (rgb1 >> 8) & 0xFF;
        int blue1 = rgb1 & 0xFF;
        int red2 = (rgb2 >> 16) & 0xFF;
        int green2 = (rgb2 >> 8) & 0xFF;
        int blue2 = rgb2 & 0xFF;
        return distance(red1, green1, blue1, red2, green2, blue2);
    }

    public static int distance(int rgb1, int r2, int g2, int b2) {
        int red1 = (rgb1 >> 16) & 0xFF;
        int green1 = (rgb1 >> 8) & 0xFF;
        int blue1 = rgb1 & 0xFF;
        return distance(red1, green1, blue1, r2, g2, b2);
    }

    public static int distance(int red1, int green1, int blue1, int red2, int green2, int blue2) {

        return (int) RgbOctreeTest.distance(red1, green1, blue1, red2, green2, blue2);

//        return Math.abs(red1 - red2) + Math.abs(green1 - green2) + Math.abs(blue1 - blue2);

//        int dr = red1 - red2;
//        int dg = green1 - green2;
//        int db = blue1 - blue2;
//        return dr * dr + dg * dg + db * db;
    }

}
