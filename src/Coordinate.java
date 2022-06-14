public class Coordinate implements Comparable<Coordinate>{
    public int x;
    public int y;

    public Coordinate() {
        this.x = 0;
        this.y = 0;
    }

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public int compareTo(Coordinate o) {
        // coords are compared based on x value so they can be sorted left to right
        return this.x - o.x;
    }
}
