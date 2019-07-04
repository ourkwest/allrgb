package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static uk.me.westmacott.Constants.MASKED;

public class MaskTester {

    public static void main(String[] args) throws IOException {

        Masker testSubject = Masker.HEXAGONAL_PATTERN;

        int[][] canvas = Data.newArray(1400, 1000);
        testSubject.mask(canvas);
        int width = canvas.length;
        int height = canvas[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, canvas[x][y] == MASKED ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        ImageIO.write(image, "png", new File("marker-test.png"));
    }

}
