package uk.me.westmacott;

import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.me.westmacott.Constants.PIXEL_COUNT;

class ColourSeries {

    /* Hue 0.0 -> 1.0 */
    static int[] allColoursInterleaved() {
        return Data.readOrWrite("allColoursInterleaved", ColourSeries::getAllColoursInterleaved);
    }

    static int[] allColoursShuffled() {
        return Data.readOrWrite("allColoursShuffled", ColourSeries::getAllColoursShuffled);
    }

    /* Sat 0.0 -> 1.0 ? */
    static int[] allColoursBySaturation() {
        return Data.readOrWrite("allColoursBySaturation", () -> ColourSeries.getAllColoursOrdered(ColourSeries.bySaturation));
    }

    private static int[] getAllColoursShuffled() {
        System.out.println("Generating colours...");
        List<Integer> allColoursAsIntegers = IntStream.range(0, PIXEL_COUNT).boxed().collect(Collectors.toList());

        System.out.println("Arranging colours...");
        Collections.shuffle(allColoursAsIntegers);

        System.out.println("Unboxing colours...");
        return allColoursAsIntegers.stream().mapToInt(i -> i).toArray();
    }

    private static final Comparator<Integer> byHue = Comparator.comparingInt(ColourSeries::getHue);
    private static final Comparator<Integer> bySaturation = Comparator.comparingInt(ColourSeries::getSaturation);

    private static int getHue(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return getHue(red, green, blue);
    }

    private static int getHue(int red, int green, int blue) {

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

    private static int getSaturation(Integer rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        return (int) (1000.0f * Color.RGBtoHSB(red, green, blue, new float[3])[1]);
    }

    private static int[] getAllColoursOrdered(Comparator<Integer> ordering) {
        System.out.println("Generating colours...");
        List<Integer> allColoursAsIntegers = IntStream.range(0, PIXEL_COUNT).boxed().collect(Collectors.toList());

        System.out.println("Arranging colours...");
        Collections.shuffle(allColoursAsIntegers);
        allColoursAsIntegers.sort(ordering);

        System.out.println("Unboxing colours...");
        return allColoursAsIntegers.stream().mapToInt(i -> i).toArray();
    }

    private static int[] getAllColoursInterleaved() {
        int[] primitiveArray = getAllColoursOrdered(byHue);
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

}
