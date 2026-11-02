package carwash.model;

import carwash.exception.ValidationException;

public class Booking implements Comparable<Booking> {
    private final Customer customer;
    private final String licensePlate;
    private final WashType washType;
    private final double price;
    private final String timeSlot;

    public Booking(Customer customer, String licensePlate, WashType washType, double price, String timeSlot)
            throws ValidationException {
        if (customer == null) {
            throw new ValidationException("Az ügyfél megadása kötelező!");
        }
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new ValidationException("A rendszám nem lehet üres!");
        }
        if (washType == null) {
            throw new ValidationException("A mosási típus megadása kötelező!");
        }
        if (timeSlot == null || timeSlot.trim().isEmpty()) {
            throw new ValidationException("Az időpont nem lehet üres!");
        }
        if (price < 0) {
            throw new ValidationException("Az ár nem lehet negatív!");
        }
        this.customer = customer;
        this.licensePlate = licensePlate.trim().toUpperCase();
        this.washType = washType;
        this.price = price;
        this.timeSlot = timeSlot.trim();
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public WashType getWashType() {
        return washType;
    }

    public double getPrice() {
        return price;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    @Override
    public int compareTo(Booking other) {
        return this.timeSlot.compareTo(other.timeSlot);
    }

    @Override
    public String toString() {
        return String.format("%s | Ügyfél: %-15s | Rendszám: %-8s | %-14s | %.0f Ft",
                timeSlot, customer.getName(), licensePlate, washType.getName(), price);
    }
}
