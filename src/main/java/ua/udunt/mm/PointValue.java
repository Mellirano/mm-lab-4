package ua.udunt.mm;

public class PointValue {

    double x;
    double y;
    double value;

    public PointValue(double x, double y, double value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("(x=%.4f, y=%.4f, f=%.4f)", x, y, value);
    }

}
