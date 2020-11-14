package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageSpitter {

    private final Path directory;
    private final Integer renderingNumber;

    public ImageSpitter() {
        this(".");
    }

    public ImageSpitter(String directory) {
        this.directory = Paths.get(directory);
        this.directory.toFile().mkdirs();
        this.renderingNumber = Data.readAndWrite("Number", () -> 0, i -> i + 1);
    }

    public String spitImage(int[][] canvas, Object suffix) throws IOException {
        int width = canvas.length;
        int height = canvas[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // TODO: is there a bulk operation for this?
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (canvas[x][y] >= 0) {
                    image.setRGB(x, y, canvas[x][y] | 0xFF000000);
                }
            }
        }
        String filename = String.format("Render_%03d-%s.png", renderingNumber, suffix);
        ImageIO.write(image, "png", directory.resolve(filename).toFile());
        return filename;
    }

    public String spitMask(int[][] canvas, Object suffix, Color maskColour) throws IOException {
        int width = canvas.length;
        int height = canvas[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (canvas[x][y] >= 0) {
                    image.setRGB(x, y, canvas[x][y] | 0xFF000000);
                } else if (canvas[x][y] == Constants.MASKED) {
                    image.setRGB(x, y, maskColour.getRGB());
                }
            }
        }
        String filename = String.format("Render_%03d-%s.png", renderingNumber, suffix);
        ImageIO.write(image, "png", directory.resolve(filename).toFile());
        return filename;
    }

}
