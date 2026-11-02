package carwash.model;

public interface DiscountStrategy {
    double calculate(double basePrice);
    String getName();
}
