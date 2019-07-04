package uk.me.westmacott;

import java.io.IOException;

import static uk.me.westmacott.Constants.GOLDEN_RATIO;
import static uk.me.westmacott.Constants.IMAGE_SIZE;

/**
 * Mountains and lettuce leaves and lightning and space.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        final int[][] canvas = Data.newArray((int) (IMAGE_SIZE * 1.1), IMAGE_SIZE);
        final AvailablePointsByTargetColour availabilities = new AvailablePointsByTargetColour();

        new MountainsLettuceLightningSpace().render(
                ColourSeries.BY_BRIGHTNESS,
                availabilities,
                Masker.HEXAGONAL_PATTERN,
                Seeder.TOP_LEFT_ISH_DOT,
                canvas,
                new ImageSpitter("renders"));
    }

}
