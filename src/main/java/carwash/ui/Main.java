package carwash.ui;

import carwash.exception.DuplicateBookingException;
import carwash.exception.ValidationException;
import carwash.model.BasicWash;
import carwash.model.Booking;
import carwash.model.DiscountStrategy;
import carwash.model.FleetDiscount;
import carwash.model.NoDiscount;
import carwash.model.PremiumWash;
import carwash.model.WashType;
import carwash.persistence.BookingRepository;
import carwash.service.BookingService;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final BookingService service = new BookingService(new BookingRepository("bookings.txt"));

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Üdvözöljük a CarWash Foglalási Rendszerben!");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Válasszon menüpontot: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleAddBooking(scanner);
                    break;
                case "2":
                    printBookings("Foglalások időpont szerint rendezve", service.getBookingsSortedByTime());
                    break;
                case "3":
                    printBookings("Foglalások ár szerint rendezve", service.getBookingsSortedByPrice());
                    break;
                case "4":
                    handleSave();
                    break;
                case "5":
                    handleLoad();
                    break;
                case "6":
                    System.out.println("Kilépés. Viszlát!");
                    running = false;
                    break;
                default:
                    System.out.println("Érvénytelen menüpont! Kérjük válasszon 1-6 között.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- MENÜ ---");
        System.out.println("1. Új foglalás rögzítése");
        System.out.println("2. Foglalások listázása (Időpont szerint)");
        System.out.println("3. Foglalások listázása (Ár szerint növekvő)");
        System.out.println("4. Foglalások mentése fájlba");
        System.out.println("5. Foglalások betöltése fájlból");
        System.out.println("6. Kilépés");
    }

    private static void handleAddBooking(Scanner scanner) {
        try {
            System.out.print("Időpont (pl. 2026-11-20 14:00): ");
            String timeSlot = scanner.nextLine();

            System.out.print("Ügyfél neve: ");
            String name = scanner.nextLine();

            System.out.print("Rendszám (pl. ABC-123): ");
            String plate = scanner.nextLine();

            System.out.println("Mosási típus:");
            System.out.println("  1 - Alap mosás (3000 Ft)");
            System.out.println("  2 - Prémium mosás (6000 Ft)");
            System.out.print("Választás [1]: ");
            String washChoice = scanner.nextLine().trim();
            WashType washType = "2".equals(washChoice) ? new PremiumWash() : new BasicWash();

            System.out.print("Céges flotta partner? (i/n) [n]: ");
            String fleetChoice = scanner.nextLine().trim();
            DiscountStrategy discount = fleetChoice.equalsIgnoreCase("i")
                    ? new FleetDiscount()
                    : new NoDiscount();

            Booking booking = service.addBooking(name, plate, washType, discount, timeSlot);
            System.out.println("\n✓ Sikeres foglalás!");
            System.out.printf("Csomag: %s | Kedvezmény: %s | Fizetendő: %.0f Ft\n",
                    booking.getWashType().getName(), discount.getName(), booking.getPrice());

        } catch (DuplicateBookingException e) {
            System.out.println("FOGLALÁSI HIBA: " + e.getMessage());
        } catch (ValidationException e) {
            System.out.println("ÉRVÉNYTELEN ADAT: " + e.getMessage());
        }
    }

    private static void printBookings(String title, List<Booking> list) {
        if (list.isEmpty()) {
            System.out.println("\n[!] Nincsenek rögzített foglalások.");
            return;
        }
        System.out.println("\n--- " + title + " ---");
        for (Booking b : list) {
            System.out.println(b);
        }
    }

    private static void handleSave() {
        try {
            service.saveToFile();
            System.out.println("✓ Foglalások elmentve a 'bookings.txt' fájlba.");
        } catch (IOException e) {
            System.out.println("Hiba mentés közben: " + e.getMessage());
        }
    }

    private static void handleLoad() {
        try {
            service.loadFromFile();
            System.out.println("✓ Foglalások betöltve (" + service.getBookings().size() + " db).");
        } catch (IOException | ValidationException e) {
            System.out.println("Hiba betöltés közben: " + e.getMessage());
        }
    }
}
