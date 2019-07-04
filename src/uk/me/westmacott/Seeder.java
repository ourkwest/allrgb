package uk.me.westmacott;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.me.westmacott.Constants.MASKED;
import static uk.me.westmacott.Constants.TAU;

public enum Seeder {

    CENTRAL_GREY_DOT() {
        public void seed(AvailablePointsByTargetColour availabilities, int[][] canvas) {
            seed(availabilities, canvas, Data.width(canvas) / 2, Data.height(canvas) / 2, Color.GRAY);
        }
    },
    TOP_LEFT_ISH_DOT() {
        public void seed(AvailablePointsByTargetColour availabilities, int[][] canvas) {
            seed(availabilities, canvas, Data.width(canvas) / 3, Data.height(canvas) / 3, Color.GRAY);
        }
    },
    BLACK_TOP_ROW() {
        public void seed(AvailablePointsByTargetColour availabilities, int[][] canvas) {
            List<Integer> list = IntStream.range(0, Data.width(canvas)).boxed().collect(Collectors.toList());
            Collections.shuffle(list);
            list.forEach(x -> seed(availabilities, canvas, x, 0, Color.BLACK));
        }
    },
    GREY_CORNERS() {
        public void seed(AvailablePointsByTargetColour availabilities, int[][] canvas) {
            int width = Data.width(canvas) - 1;
            int height = Data.height(canvas) - 1;
            seed(availabilities, canvas, 0, 0, Color.GRAY);
            seed(availabilities, canvas, width, 0, Color.GRAY);
            seed(availabilities, canvas, 0, height, Color.GRAY);
            seed(availabilities, canvas, width, height, Color.GRAY);
        }
    },
    CIRCLE_OF_COLOUR() {
        public void seed(AvailablePointsByTargetColour availabilities, int[][] canvas) {
            int width = Data.width(canvas);
            int height = Data.height(canvas);
            int radius = (int) (Math.min(width, height) * 0.45);
            for (double theta = 0; theta < TAU; theta += 0.1) {
                int x = (width / 2) + (int)(radius * Math.sin(theta));
                int y = (height / 2) + (int)(radius * Math.cos(theta));
                Color colour = Color.getHSBColor((float) (theta / TAU), 1.0f, 1.0f);
                seed(availabilities, canvas, x, y, colour);
            }
        }
    };

    public abstract void seed(AvailablePointsByTargetColour availablePointsByTargetColour, int[][] canvas);

    static void seed(AvailablePointsByTargetColour availablePointsByTargetColour, int[][] canvas, int x, int y, Color colour) {
        if (canvas[x][y] != MASKED) {
            availablePointsByTargetColour.add(colour.getRGB() & 0xFFFFFF, new Point(x,y));
        }
    }

}
