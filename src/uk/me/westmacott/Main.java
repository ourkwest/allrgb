package uk.me.westmacott;

import java.awt.*;
import java.io.IOException;

import static uk.me.westmacott.Constants.IMAGE_SIZE;
import static uk.me.westmacott.Constants.TAU;

/**
 * Mountains and lettuce leaves and lightning and space.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        final Canvas canvas = new Canvas(IMAGE_SIZE * 2, IMAGE_SIZE);
        final Availabilities availabilities = new Availabilities();

        System.out.println("Seeding image...");
//        availabilities.add(Color.GRAY.getRGB() & 0xFFFFFF, new Point(IMAGE_SIZE / 2, IMAGE_SIZE / 2));

//        {
//            List<Integer> list = IntStream.range(0, imageWidth).mapToObj(i -> i).collect(Collectors.toList());
//            Collections.shuffle(list);
//            list.stream().forEach(x -> availabilities.add(Color.BLACK.getRGB() & 0xFFFFFF, new Point(x, 0)));
//        }

        {
            for (double theta = 0; theta < TAU; theta += 0.1) {
                int x = (canvas.getWidth() / 2) + (int)(2000 * Math.sin(theta));
                int y = (canvas.getHeight() / 2) + (int)(2000 * Math.cos(theta));
                int colour = Color.getHSBColor((float) (theta / TAU), 1.0f, 1.0f).getRGB() & 0xFFFFFF;
                availabilities.add(colour, new Point(x, y));
            }
        }


        new MountainsLettuceLightningSpace().render(ColourSeries.SHUFFLED, availabilities, canvas, new ImageSpitter());

    }

}
