package uk.me.westmacott;

class Constants {

    private static final int COLOUR_CUBE_SIZE = 256;
    static final int IMAGE_SIZE = (int) Math.pow(COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE, 0.5);
    static final int PIXEL_COUNT = COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE;
    static final int DEBUG_EVERY = PIXEL_COUNT / 32;
    static final int SNAPSHOT_EVERY = PIXEL_COUNT / 8;
    static final int UNSET = -1;
    static final int MASKED = -2;

    static final double TAU = 2.0 * Math.PI;
    static final double GOLDEN_RATIO = 1.61803398875;


}
