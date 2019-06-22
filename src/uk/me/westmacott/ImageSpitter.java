package uk.me.westmacott;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static uk.me.westmacott.Constants.NUMBER;

class ImageSpitter {

    void spitImage(Canvas canvas, Object suffix) throws IOException {
        String filename = String.format("Render_%03d-%s", NUMBER, suffix);
        ImageIO.write(canvas.toImage(), "png", new File("./" + filename + ".png"));
    }

}
