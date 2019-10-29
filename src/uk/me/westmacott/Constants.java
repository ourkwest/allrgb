package uk.me.westmacott;

public class Constants {

    private static final int COLOUR_CUBE_SIZE = 256;
    public static final int IMAGE_SIZE = (int) Math.pow(COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE, 0.5);
    public static final int PIXEL_COUNT = COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE * COLOUR_CUBE_SIZE;
    static final int DEBUG_EVERY = PIXEL_COUNT / 32;
    static final int SNAPSHOT_EVERY = PIXEL_COUNT / 8;
    public static final int UNSET = -1;
    public static final int MASKED = -2;

    public static final double TAU = 2.0 * Math.PI;
    public static final double GOLDEN_RATIO = 1.61803398875;


}
