package carwash.model;

public class PremiumWash extends WashType {
    @Override
    public String getName() {
        return "Prémium mosás";
    }

    @Override
    public double getBasePrice() {
        return 6000.0;
    }
}
