package model;

import java.util.Objects;

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

    public Point resize(double maxLength) {
        double curLength = length();
        double scale = curLength / maxLength;
        return new Point(x / scale, y / scale);
    }

    public double distTo(Point to) {
        return new Point(to.x - x, to.y - y).length();
    }

    public Point add(Point oth) {
        return new Point(x + oth.x, y + oth.y);
    }

    public Point sub(Point oth) {
        return new Point(x - oth.x, y - oth.y);
    }

    public Point mul(double val) {
        return new Point(x * val, y * val);
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return Double.compare(x, point.x) == 0 && Double.compare(y, point.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Point duplicate() {
        return new Point(x, y);
    }
}
