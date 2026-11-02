package carwash.model;

public abstract class WashType {
    public abstract String getName();
    public abstract double getBasePrice();

    @Override
    public String toString() {
        return getName() + " (" + (int) getBasePrice() + " Ft)";
    }
}
