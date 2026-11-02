package carwash.model;

public class NoDiscount implements DiscountStrategy {
    @Override
    public double calculate(double basePrice) {
        return basePrice;
    }

    @Override
    public String getName() {
        return "Normál";
    }
}
