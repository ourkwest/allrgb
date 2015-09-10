package uk.me.westmacott;

import java.util.Arrays;

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
    int[][] counts = new int[8][];
    {
        int size = 1;
        for (int depth = 0; depth < 8; depth++) {
            counts[depth] = new int[size];
            size *= 8;
        }
//        System.out.println(colourCount);
    }

    public static final int UNSET = -1;
    int[] colours = new int[colourCount];
    {
        Arrays.fill(colours, UNSET);
    }


    void mapToIndex(int depth0, int index0, int r, int g, int b) {

        int depth1 = depth0 + 1;

        int mask = 0b10000000 >> depth0;
        int index1 =
                8 * index0
                        + (r & mask)
                        + 2 * (g & mask)
                        + 4 * (b & mask);

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
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int index = 0;
        for (int depth = 0; depth < 8; depth++) {
            counts[depth][index]++;
            int mask = 0b10000000 >> depth;
            index = 8 * index
                    + ((r & mask) == 0 ? 0 : 4)
                    + ((g & mask) == 0 ? 0 : 2)
                    + ((b & mask) == 0 ? 0 : 1);
        }
        colours[index] = rgb;
    }

    void remove(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int index = 0;
        for (int depth = 0; depth < 8; depth++) {
            counts[depth][index]--;
            int mask = 0b10000000 >> depth;
            index = 8 * index
                    + ((r & mask) == 0 ? 0 : 4)
                    + ((g & mask) == 0 ? 0 : 2)
                    + ((b & mask) == 0 ? 0 : 1);
        }
        colours[index] = UNSET;
    }

    int probablyNearTo(int rgb) {

        int r = (rgb >> 16) & 0xFF; // mask is unnecessary for red?
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int goodIndex = 0, goodDepth = 0;

        int index = 0;
        for (int depth = 0; depth < 8; depth++) {
            if (counts[depth][index] > 0) {
                goodIndex = index;
                goodDepth = depth;
                int mask = 0b10000000 >> depth;
                index = 8 * index
                        + ((r & mask) == 0 ? 0 : 4)
                        + ((g & mask) == 0 ? 0 : 2)
                        + ((b & mask) == 0 ? 0 : 1);
            } else {
                break;
            }
        }

        int min = goodIndex;
        int max = goodIndex;
        for (int depth = goodDepth; depth < 8; depth++) {
            min = 8 * min;
            max = 8 * max + 7;
        }

        int best = 0;
        int leastDistance = Integer.MAX_VALUE;
        for (int i = min; i <= max; i++) {
            if (colours[i] != UNSET) {
                int distance = distance(colours[i], rgb);
                if (distance < leastDistance) {
                    leastDistance = distance;
                    best = colours[i];
                }
            }
        }

        return best;

        // correct alternative?

        // descend until count == 0
        // ascend until count is > 0
        // measure distance of all children to target (keep best)
        // measure distance from target to boundary
        // for each boundary that is closer, back up and explore???

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

    public static int distance(int red1, int green1, int blue1, int red2, int green2, int blue2) {
        int dr = red1 - red2;
        int dg = green1 - green2;
        int db = blue1 - blue2;
        return dr * dr + dg * dg + db * db;
    }

}
