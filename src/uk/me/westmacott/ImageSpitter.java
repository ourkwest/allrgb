package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface ImageSpitter {

    File spitImage(int[][] canvas, boolean forceSpit) throws IOException;
    File spitMask(int[][] canvas, Color maskColour) throws IOException;

    static ImageSpitter forDirectory(String directory) {
        return forDirectory(directory, true);
    }

    static ImageSpitter forDirectory(String directory, boolean overwrite) {
        return new DirectorySpitter(directory, overwrite);
    }

    static ImageSpitter onceEvery(Integer frames, ImageSpitter decoree) {
        return new ImageSpitter() {
            int counter = 0;
            @Override
            public File spitImage(int[][] canvas, boolean forceSpit) throws IOException {
                if ((counter++ % frames) == 0 || forceSpit) {
                    return decoree.spitImage(canvas, forceSpit);
                } else {
                    return null;
                }
            }

            @Override
            public File spitMask(int[][] canvas, Color maskColour) throws IOException {
                return decoree.spitMask(canvas, maskColour);
            }
        };
    }

    static ImageSpitter defaultSpitter() {
        return onceEvery(Constants.SNAPSHOT_EVERY, forDirectory("clj-renders"));
    }

    class DirectorySpitter implements ImageSpitter {

        private final Path directory;
        private final Integer renderingNumber;
        private final boolean overwrite;
        private Integer sequenceNumber;

        public DirectorySpitter(String directory, boolean overwrite) {
            this.directory = Paths.get(directory);
            this.directory.toFile().mkdirs();
            this.renderingNumber = Data.readAndWrite("Number", () -> 0, i -> i + 1);
            this.overwrite = overwrite;
            sequenceNumber = 0;
        }

        public File spitImage(int[][] canvas, boolean forceSpit) throws IOException {
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
            String filename = overwrite ?
                String.format("Render_%03d.png", renderingNumber) :
                String.format("Render_%03d-%04d.png", renderingNumber, sequenceNumber++);
            File outputFile = directory.resolve(filename).toFile();
            ImageIO.write(image, "png", outputFile);
            return outputFile;
        }

        public File spitMask(int[][] canvas, Color maskColour) throws IOException {
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
            String filename = String.format("Render_%03d-mask.png", renderingNumber);
            File outputFile = directory.resolve(filename).toFile();
            ImageIO.write(image, "png", outputFile);
            return outputFile;
        }
    }

}
