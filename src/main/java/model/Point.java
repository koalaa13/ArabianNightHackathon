package model;

public class Point {
    public double x;
    public double y;

    public Point() {
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Point shrink(double maxLength) {
        double curLength = length();
        double scale = curLength / maxLength;
        if (scale > 1) {
            return new Point(x / scale, y / scale);
        } else {
            return this;
        }
    }

    public double distTo(Point to) {
        return new Point(to.x - x, to.y - y).length();
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
