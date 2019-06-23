package uk.me.westmacott;

import java.io.IOException;

import static uk.me.westmacott.Constants.GOLDEN_RATIO;
import static uk.me.westmacott.Constants.IMAGE_SIZE;

/**
 * Mountains and lettuce leaves and lightning and space.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        final int[][] canvas = Data.newArray((int) (IMAGE_SIZE * GOLDEN_RATIO), IMAGE_SIZE);
        final AvailablePointsByTargetColour availabilities = new AvailablePointsByTargetColour();

        new MountainsLettuceLightningSpace().render(
                ColourSeries.INTERLEAVED,
                availabilities,
                Masker.GAPPED_HEXAGON,
                Seeder.CIRCLE_OF_COLOUR,
                canvas,
                new ImageSpitter("renders"));
    }

}
