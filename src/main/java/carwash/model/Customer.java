package carwash.model;

import carwash.exception.ValidationException;

public class Customer {
    private final String name;

    public Customer(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Az ügyfél neve nem lehet üres!");
        }
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
