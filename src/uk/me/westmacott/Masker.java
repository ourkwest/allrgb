package uk.me.westmacott;

import java.util.function.BiFunction;

import static uk.me.westmacott.Constants.MASKED;
import static uk.me.westmacott.Constants.UNSET;

public enum Masker {

    BIG_DOT() {
        @Override
        public void mask(int[][] canvas) {
            int width = Data.width(canvas);
            int height = Data.height(canvas);
            int midX = width / 2;
            int midY = height / 2;
            int radius = Math.min(width, height) / 3;
            int rSquared = radius * radius;
            maskWhere(canvas, (x, y) -> {
                int offsetX = x - midX;
                int offsetY = y - midY;
                return (offsetX * offsetX + offsetY * offsetY) < rSquared;
            });
        }
    },
    GAPPED_RING() {
        @Override
        public void mask(int[][] canvas) {
            int width = Data.width(canvas);
            int height = Data.height(canvas);
            int midX = width / 2;
            int midY = height / 2;
            int factor = Math.min(width, height);
            int innerRadius = (int) (factor / 3.5);
            int innerLimit = innerRadius * innerRadius;
            int outerRadius = factor / 3;
            int outerLimit = outerRadius * outerRadius;
            maskWhere(canvas, (x, y) -> {
                int offsetX = x - midX;
                int offsetY = y - midY;
                int rSquared = (offsetX * offsetX + offsetY * offsetY);
                return innerLimit < rSquared && rSquared < outerLimit
                        && Math.abs(offsetX) > factor / 10
                        && Math.abs(offsetY) > factor / 10;
            });
        }
    };

    public abstract void mask(int[][] canvas);

    public static void maskWhere(int[][] canvas, BiFunction<Integer,Integer,Boolean> predicate) {
        setWhere(canvas, predicate, MASKED);
    }

    public static void unmaskWhere(int[][] canvas, BiFunction<Integer,Integer,Boolean> predicate) {
        setWhere(canvas, predicate, UNSET);
    }

    public static void setWhere(int[][] canvas, BiFunction<Integer,Integer,Boolean> predicate, int value) {
        int width = Data.width(canvas);
        int height = Data.height(canvas);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (predicate.apply(x, y)) {
                    canvas[x][y] = value;
                }
            }
        }
    }

}
