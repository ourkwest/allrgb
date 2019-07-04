package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static uk.me.westmacott.Constants.*;

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
                        && Math.abs(offsetX) > factor / 20
                        && Math.abs(offsetY) > factor / 20;
            });
        }
    },
    DOUBLE_GAPPED_RING() {
        @Override
        public void mask(int[][] canvas) {}
    },
    GAPPED_HEXAGON() {
        @Override
        public void mask(int[][] canvas) {

            int width = Data.width(canvas);
            int height = Data.height(canvas);
            int factor = Math.min(width, height);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0,0,width,height);
            graphics.setColor(Color.BLACK);

            List<Point> points = Data.circle(width/2, height/2, factor/3,0, TAU/6).collect(Collectors.toList());
            int[] xs = points.stream().mapToInt(p -> p.x).toArray();
            int[] ys = points.stream().mapToInt(p -> p.y).toArray();
            graphics.setStroke(new BasicStroke(factor / 20));
            graphics.drawPolygon(xs, ys, points.size());

            graphics.setColor(Color.WHITE);
            graphics.setStroke(new BasicStroke(factor / 19));
            for (int i = 0; i < xs.length; i++) {
                graphics.drawLine(
                        (int)(xs[i] * 0.6 + xs[(i+1)%xs.length] * 0.4),
                        (int)(ys[i] * 0.6 + ys[(i+1)%xs.length] * 0.4),
                        (int)(xs[i] * 0.4 + xs[(i+1)%xs.length] * 0.6),
                        (int)(ys[i] * 0.4 + ys[(i+1)%xs.length] * 0.6)
                );
            }

            try {
                ImageIO.write(image, "png", new File("marker-test-0.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            maskWhere(canvas, (x,y) -> image.getRGB(x, y) == Color.BLACK.getRGB());
        }
    },
    HEXAGONAL_PATTERN() {
        @Override
        public void mask(int[][] canvas) {

            int width = Data.width(canvas);
            int height = Data.height(canvas);
            int factor = Math.min(width, height);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0,0,width,height);
            graphics.setColor(Color.BLACK);

            List<Point> points = Data.circle(width/2, height/2, factor/3,0, TAU/6).collect(Collectors.toList());
            int[] xs = points.stream().mapToInt(p -> p.x).toArray();
            int[] ys = points.stream().mapToInt(p -> p.y).toArray();
            graphics.setStroke(new BasicStroke(factor / 20));
            graphics.drawPolygon(xs, ys, points.size());

            graphics.setColor(Color.WHITE);
            graphics.setStroke(new BasicStroke(factor / 19));
            for (int i = 0; i < xs.length; i++) {
                graphics.drawLine(
                        (int)(xs[i] * 0.6 + xs[(i+1)%xs.length] * 0.4),
                        (int)(ys[i] * 0.6 + ys[(i+1)%xs.length] * 0.4),
                        (int)(xs[i] * 0.4 + xs[(i+1)%xs.length] * 0.6),
                        (int)(ys[i] * 0.4 + ys[(i+1)%xs.length] * 0.6)
                );
            }

            graphics.setColor(Color.BLACK);
            points = Data.circle(width/2, height/2, (int) (factor/2.5),TAU/12, TAU/6).collect(Collectors.toList());
            xs = points.stream().mapToInt(p -> p.x).toArray();
            ys = points.stream().mapToInt(p -> p.y).toArray();
            int size = factor / 20;
            for (int i = 0; i < xs.length; i++) {
                graphics.fillOval(xs[i] - size, ys[i] - size, 2*size, 2*size);
            }

            points = Data.circle(width/2, height/2, factor/5,TAU/12, TAU/6).collect(Collectors.toList());
            xs = points.stream().mapToInt(p -> p.x).toArray();
            ys = points.stream().mapToInt(p -> p.y).toArray();
            size = factor / 26;
            for (int i = 0; i < xs.length; i++) {
                graphics.fillOval(xs[i] - size, ys[i] - size, 2*size, 2*size);
            }

            try {
                ImageIO.write(image, "png", new File("marker-test-0.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            maskWhere(canvas, (x,y) -> image.getRGB(x, y) == Color.BLACK.getRGB());
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
