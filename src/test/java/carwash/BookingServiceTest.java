package carwash;

import carwash.exception.DuplicateBookingException;
import carwash.exception.ValidationException;
import carwash.model.BasicWash;
import carwash.model.Booking;
import carwash.model.Customer;
import carwash.model.FleetDiscount;
import carwash.model.NoDiscount;
import carwash.model.PremiumWash;
import carwash.persistence.BookingRepository;
import carwash.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingRepository repository;
    private BookingService service;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        File tempFile = tempDir.resolve("test_bookings.txt").toFile();
        repository = new BookingRepository(tempFile.getAbsolutePath());
        service = new BookingService(repository);
    }

    @Test
    void testAddBookingWithoutDiscount() throws DuplicateBookingException, ValidationException {
        Booking booking = service.addBooking("Kovács János", "ABC-123", new BasicWash(),
                new NoDiscount(), "2026-11-20 10:00");

        assertNotNull(booking);
        assertEquals("ABC-123", booking.getLicensePlate());
        assertEquals("Kovács János", booking.getCustomer().getName());
        assertEquals(3000.0, booking.getPrice(), 0.001);
        assertEquals(1, service.getBookings().size());
    }

    @Test
    void testAddBookingWithFleetDiscount() throws DuplicateBookingException, ValidationException {
        // PremiumWash (6000 Ft) - 20% = 4800 Ft
        Booking booking = service.addBooking("Nagy Flotta Kft.", "FLT-001", new PremiumWash(),
                new FleetDiscount(), "2026-11-20 11:00");

        assertEquals(4800.0, booking.getPrice(), 0.001);
    }

    @Test
    void testDuplicateTimeSlotThrowsException() throws DuplicateBookingException, ValidationException {
        service.addBooking("Kovács János", "ABC-123", new BasicWash(),
                new NoDiscount(), "2026-11-20 12:00");

        assertThrows(DuplicateBookingException.class, () ->
                service.addBooking("Kiss Péter", "XYZ-789", new PremiumWash(),
                        new NoDiscount(), "2026-11-20 12:00")
        );
    }

    @Test
    void testValidationFailsForBlankFields() {
        assertThrows(ValidationException.class, () ->
                service.addBooking("", "ABC-123", new BasicWash(),
                        new NoDiscount(), "2026-11-20 14:00")
        );

        assertThrows(ValidationException.class, () ->
                service.addBooking("Teszt Elek", "   ", new BasicWash(),
                        new NoDiscount(), "2026-11-20 14:00")
        );
    }

    @Test
    void testSortByTime() throws DuplicateBookingException, ValidationException {
        service.addBooking("C", "CCC-333", new BasicWash(), new NoDiscount(), "2026-11-20 14:00");
        service.addBooking("A", "AAA-111", new BasicWash(), new NoDiscount(), "2026-11-20 09:00");
        service.addBooking("B", "BBB-222", new BasicWash(), new NoDiscount(), "2026-11-20 11:00");

        List<Booking> sorted = service.getBookingsSortedByTime();
        assertEquals("2026-11-20 09:00", sorted.get(0).getTimeSlot());
        assertEquals("2026-11-20 11:00", sorted.get(1).getTimeSlot());
        assertEquals("2026-11-20 14:00", sorted.get(2).getTimeSlot());
    }

    @Test
    void testSortByPrice() throws DuplicateBookingException, ValidationException {
        service.addBooking("Drága", "AAA-111", new PremiumWash(), new NoDiscount(), "2026-11-20 09:00"); // 6000 Ft
        service.addBooking("Olcsó", "BBB-222", new BasicWash(), new FleetDiscount(), "2026-11-20 10:00"); // 2400 Ft
        service.addBooking("Közepes", "CCC-333", new BasicWash(), new NoDiscount(), "2026-11-20 11:00"); // 3000 Ft

        List<Booking> sorted = service.getBookingsSortedByPrice();
        assertEquals(2400.0, sorted.get(0).getPrice(), 0.001);
        assertEquals(3000.0, sorted.get(1).getPrice(), 0.001);
        assertEquals(6000.0, sorted.get(2).getPrice(), 0.001);
    }

    @Test
    void testPersistenceSaveAndLoad() throws IOException, DuplicateBookingException, ValidationException {
        service.addBooking("Teszt Ügyfél", "TST-999", new PremiumWash(),
                new FleetDiscount(), "2026-11-20 15:00");
        service.saveToFile();

        BookingService newService = new BookingService(repository);
        newService.loadFromFile();

        assertEquals(1, newService.getBookings().size());
        Booking loaded = newService.getBookings().get(0);
        assertEquals("Teszt Ügyfél", loaded.getCustomer().getName());
        assertEquals("TST-999", loaded.getLicensePlate());
        assertEquals("Prémium mosás", loaded.getWashType().getName());
        assertEquals(4800.0, loaded.getPrice(), 0.001);
        assertEquals("2026-11-20 15:00", loaded.getTimeSlot());
    }
}
