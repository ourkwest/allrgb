package uk.me.westmacott;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import static uk.me.westmacott.Constants.UNSET;

class Canvas {

    private final int[][] data;
    private final int width;
    private final int height;

    Canvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = Data.newArray(width, height);
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    int getPixel(Point p) {
        return data[p.x][p.y];
    }

    void setPixel(Point p, int colour) {
        data[p.x][p.y] = colour;
    }

    RenderedImage toImage() {
        int width = data.length;
        int height = data[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // TODO: is there a bulk operation for this?
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (data[x][y] != UNSET) {
                    image.setRGB(x, y, data[x][y] | 0xFF000000);
                }
            }
        }
        return image;
    }
}
