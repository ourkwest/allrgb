package uk.me.westmacott;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.me.westmacott.Constants.TAU;

public enum Seeder {

    CENTRAL_GREY_DOT() {
        public void seed(Availabilities availabilities, int width, int height) {
                availabilities.add(
                        Color.GRAY.getRGB() & 0xFFFFFF,
                        new Point(width / 2, height / 2));
        }
    },
    BLACK_TOP_ROW() {
        public void seed(Availabilities availabilities, int width, int height) {
            List<Integer> list = IntStream.range(0, width).boxed().collect(Collectors.toList());
            Collections.shuffle(list);
            list.forEach(x -> availabilities.add(Color.BLACK.getRGB() & 0xFFFFFF, new Point(x, 0)));
        }
    },
    CIRCLE_OF_COLOUR() {
        public void seed(Availabilities availabilities, int width, int height) {
            int radius = (int) (Math.min(width, height) * 0.9);
            for (double theta = 0; theta < TAU; theta += 0.1) {
                int x = (width / 2) + (int)(radius * Math.sin(theta));
                int y = (height / 2) + (int)(radius * Math.cos(theta));
                int colour = Color.getHSBColor((float) (theta / TAU), 1.0f, 1.0f).getRGB() & 0xFFFFFF;
                availabilities.add(colour, new Point(x, y));
            }

        }
    };

    public abstract void seed(Availabilities availabilities, int width, int height);

}
