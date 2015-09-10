package uk.me.westmacott;

public class ColourMap<V> {

    Node<V> cursor = null;

    public void put(int colour, V value) {
        if (cursor == null) {
            cursor = new Node<>(colour, value);
            return;
        }
        if (cursor.colour == colour) { // not actually useful for this use case.
            cursor.value = value;
            return;
        }
//        cursor = cursor.locate(colour);
    }

    public void remove(int colour) {

    }

    public Node<V> getNearest(int colour) {

        return null;
    }


    public static class Node<V> {

        private final Node[] neighbours = new Node[8];
        public final int colour;
        public V value;

        public Node(int colour, V value) {
            this.colour = colour;
            this.value = value;
        }

        private Node stepTowards(int target) {
            int octant = getOctant(colour, target);
            Node neighbour = neighbours[octant];
            if (neighbour == null || distance(colour, target) <= distance(neighbour.colour, target)) {
                return this;
            }
            return neighbour;
        } // this for walk towards - then o a single iteration of checking all neighbours to find closest


        // Damnit I don't think that this works!
        private Node closestTo(int target) {
            Node a = this;
            Node b = null;
            while (a != b) {
                b = a;
                a = a.closerTo(target); // damnit I don't think that this works!
            }
            return a; // or b!
        }

        private Node closerTo(int target) {
            int distance = distance(colour, target);
            Node result = this;
            for (Node neighbour : neighbours) {
                int neighbourDistance = distance(neighbour.colour, target);
                if (neighbourDistance < distance) {
                    distance = neighbourDistance;
                    result = neighbour;
                }
            }
            return result;
        }

        public static int distance(int rgb1, int rgb2) {
            int red1 = (rgb1 >> 16) & 0xFF;
            int green1 = (rgb1 >> 8) & 0xFF;
            int blue1 = rgb1 & 0xFF;
            int red2 = (rgb2 >> 16) & 0xFF;
            int green2 = (rgb2 >> 8) & 0xFF;
            int blue2 = rgb2 & 0xFF;
            return distance(red1, green1, blue1, red2, green2, blue2);
        }

        public static int distance(int red1, int green1, int blue1, int red2, int green2, int blue2) {
            int dr = red1 - red2;
            int dg = green1 - green2;
            int db = blue1 - blue2;
            return dr * dr + dg * dg + db * db;
        }

        private static int getOctant(int colourA, int colourB) {
            int red = (0xFF0000 & colourA) > (0xFF0000 & colourB) ? 1 : 0;
            int grn = (0x00FF00 & colourA) > (0x00FF00 & colourB) ? 2 : 0;
            int blu = (0x0000FF & colourA) > (0x0000FF & colourB) ? 4 : 0;
            return red + grn + blu;
        }
    }
}
