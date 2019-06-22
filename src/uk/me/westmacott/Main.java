package uk.me.westmacott;

import java.io.IOException;

import static uk.me.westmacott.Constants.IMAGE_SIZE;

/**
 * Mountains and lettuce leaves and lightning and space.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        final int[][] canvas = Data.newArray(IMAGE_SIZE * 2, IMAGE_SIZE);
        final Availabilities availabilities = new Availabilities();

        new MountainsLettuceLightningSpace().render(
                ColourSeries.INTERLEAVED,
                availabilities,
                Masker.BIG_DOT, Seeder.BLACK_TOP_ROW,
                canvas,
                new ImageSpitter("renders"));
    }

}
