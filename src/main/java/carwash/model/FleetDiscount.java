package carwash.model;

public class FleetDiscount implements DiscountStrategy {
    @Override
    public double calculate(double basePrice) {
        return basePrice * 0.80; // 20% flottakedvezmény
    }

    @Override
    public String getName() {
        return "Flotta (-20%)";
    }
}
