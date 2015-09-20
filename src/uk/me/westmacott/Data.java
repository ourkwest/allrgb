package uk.me.westmacott;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Supplier;

public class Data {

    private static Path path = Paths.get(".", "data");
    static {
        path.toFile().mkdir();
    }

    public static <T> T readOrWrite(String name, Supplier<T> supplier) {
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

    public static <T> T readAndWrite(String name, Supplier<T> supplier, Function<T,T> mutator) {
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

    public static int[] newArray(int initialValue, int size) {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = initialValue;
        }
        return result;
    }

    public static int[][] newArray(int initialValue, int size0, int size1) {
        int[][] result = new int[size0][size1];
        for (int i = 0; i < size0; i++) {
            result[i] = newArray(initialValue, size1);
        }
        return result;
    }

    public static int[][][] newArray(int initialValue, int size0, int size1, int size2) {
        int[][][] result = new int[size0][size1][size2];
        for (int i = 0; i < size0; i++) {
            result[i] = newArray(initialValue, size1, size2);
        }
        return result;
    }

}
