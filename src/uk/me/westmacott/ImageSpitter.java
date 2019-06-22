package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.me.westmacott.Constants.NUMBER;
import static uk.me.westmacott.Constants.UNSET;

class ImageSpitter {

    private final Path directory;

    ImageSpitter() {
        this(".");
    }

    ImageSpitter(String directory) {
        this.directory = Paths.get(directory);
        this.directory.toFile().mkdirs();
    }

    void spitImage(int[][] canvas, Object suffix) throws IOException {
        int width = canvas.length;
        int height = canvas[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // TODO: is there a bulk operation for this?
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (canvas[x][y] != UNSET) {
                    image.setRGB(x, y, canvas[x][y] | 0xFF000000);
                }
            }
        }
        String filename = String.format("Render_%03d-%s.png", NUMBER, suffix);
        ImageIO.write(image, "png", directory.resolve(filename).toFile());
    }

}
