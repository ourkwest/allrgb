package uk.me.westmacott;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.me.westmacott.Constants.UNSET;

/**
 * Make it easy to stash data in the local file system.
 */
class Data {

    private static Path path = Paths.get(".", "data");
    static {
        path.toFile().mkdir();
    }

    static <T> T readOrWrite(String name, Supplier<T> supplier) {
        File file = path.resolve(Paths.get(name + ".data")).toFile();
        T data;
        try {
            data = read(file);
            System.out.println("Read '" + name + "'.");
        }
        catch (Exception e) {
            data = write(supplier.get(), file);
            System.out.println("Wrote '" + name + "'.");
        }
        return data;
    }

    static <T> T readAndWrite(String name, Supplier<T> supplier, Function<T, T> mutator) {
        File file = path.resolve(Paths.get(name + ".data")).toFile();
        T data;
        try {
            data = read(file);
            System.out.println("Read '" + name + "'.");
        }
        catch (Exception e) {
            data = supplier.get();
        }
        data = write(mutator.apply(data), file);
        System.out.println("Wrote '" + name + "'.");
        return data;
    }

    private static <T> T write(T data, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    private static <T> T read(File file) throws IOException, ClassNotFoundException {
        T data;FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);
        data = (T) ois.readObject();
        return data;
    }

    private static int[] newArray(int size) {
        int[] result = new int[size];
        Arrays.fill(result, UNSET);
        return result;
    }

    static int[][] newArray(int size0, int size1) {
        int[][] result = new int[size0][size1];
        for (int i = 0; i < size0; i++) {
            result[i] = newArray(size1);
        }
        return result;
    }

    static int width(int[][] canvas) {
        return canvas.length;
    }

    static int height(int[][] canvas) {
        return canvas[0].length;
    }

    static String formatTime(long millis) {
        double duration = millis;
        String unit = "Millisecond";
        if (duration >= 1000) {
            duration /= 1000;
            unit = "Second";
            if (duration >= 60) {
                duration /= 60;
                unit = "Minute";
                if (duration >= 60) {
                    duration /= 60;
                    unit = "Hour";
                }
            }
        }
        if (duration > 1) {
            unit += "s";
        }
        return String.format("%4.2f %s", duration, unit);
    }

}
