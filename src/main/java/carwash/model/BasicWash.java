package carwash.model;

public class BasicWash extends WashType {
    @Override
    public String getName() {
        return "Alap mosás";
    }

    @Override
    public double getBasePrice() {
        return 3000.0;
    }
}
