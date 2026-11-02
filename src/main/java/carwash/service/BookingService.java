package carwash.service;

import carwash.exception.DuplicateBookingException;
import carwash.exception.ValidationException;
import carwash.model.Booking;
import carwash.model.Customer;
import carwash.model.DiscountStrategy;
import carwash.model.WashType;
import carwash.persistence.BookingRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookingService {
    private final List<Booking> bookings;
    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
        this.bookings = new ArrayList<>();
    }

    public Booking addBooking(String customerName, String licensePlate, WashType washType,
                              DiscountStrategy discountStrategy, String timeSlot)
            throws DuplicateBookingException, ValidationException {

        // Ütközésvizsgálat (domain szabály)
        for (Booking b : bookings) {
            if (b.getTimeSlot().equalsIgnoreCase(timeSlot.trim())) {
                throw new DuplicateBookingException("Erre az időpontra már létezik foglalás: " + timeSlot);
            }
        }

        Customer customer = new Customer(customerName);
        double finalPrice = discountStrategy.calculate(washType.getBasePrice());
        Booking booking = new Booking(customer, licensePlate, washType, finalPrice, timeSlot);
        bookings.add(booking);
        return booking;
    }

    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    }

    public List<Booking> getBookingsSortedByTime() {
        List<Booking> copy = new ArrayList<>(bookings);
        Collections.sort(copy); // Comparable<Booking> alapján
        return copy;
    }

    public List<Booking> getBookingsSortedByPrice() {
        List<Booking> copy = new ArrayList<>(bookings);
        copy.sort(Comparator.comparingDouble(Booking::getPrice)); // Comparator lambda alapján
        return copy;
    }

    public void saveToFile() throws IOException {
        repository.save(bookings);
    }

    public void loadFromFile() throws IOException, ValidationException {
        List<Booking> loaded = repository.load();
        bookings.clear();
        bookings.addAll(loaded);
    }
}
