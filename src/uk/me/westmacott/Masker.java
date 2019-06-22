package uk.me.westmacott;

import java.util.function.BiFunction;

import static uk.me.westmacott.Constants.MASKED;

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

//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height; y++) {
//                    int offsetX = x - midX;
//                    int offsetY = y - midY;
//                    if ((offsetX * offsetX + offsetY * offsetY) < rSquared) {
//                        canvas[x][y] = MASKED;
//                    }
//                }
//            }
        }
    };

    public abstract void mask(int[][] canvas);

    public static void maskWhere(int[][] canvas, BiFunction<Integer,Integer,Boolean> predicate) {
        int width = Data.width(canvas);
        int height = Data.height(canvas);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (predicate.apply(x, y)) {
                    canvas[x][y] = MASKED;
                }
            }
        }
    }

}
