package carwash;

import java.io.*;
import java.util.*;

// 1. Egységbezárás (Encapsulation)
class Booking {
    private String licensePlate;
    private String serviceName;
    private double price;
    private String timeSlot;

    public Booking(String licensePlate, String serviceName, double price, String timeSlot) {
        this.licensePlate = licensePlate;
        this.serviceName = serviceName;
        this.price = price;
        this.timeSlot = timeSlot;
    }

    public String getLicensePlate() { return licensePlate; }
    public String getServiceName() { return serviceName; }
    public double getPrice() { return price; }
    public String getTimeSlot() { return timeSlot; }

    @Override
    public String toString() {
        return String.format("%s | Rendszám: %-8s | %-12s | %.0f Ft", timeSlot, licensePlate, serviceName, price);
    }
}

// 2. Absztrakció és Öröklődés (Abstraction & Inheritance)
abstract class WashType {
    public abstract String getName();
    public abstract double getBasePrice();
}

class BasicWash extends WashType {
    @Override public String getName() { return "Alap mosás"; }
    @Override public double getBasePrice() { return 3000; }
}

class PremiumWash extends WashType {
    @Override public String getName() { return "Prémium mosás"; }
    @Override public double getBasePrice() { return 6000; }
}

// 4. Saját Kivétel (Custom Exception)
class DuplicateBookingException extends Exception {
    public DuplicateBookingException(String message) {
        super(message);
    }
}

// 6. Tervezési Minta - Strategy (Strategy Pattern)
interface DiscountStrategy {
    double calculate(double price);
}

class NoDiscount implements DiscountStrategy {
    @Override public double calculate(double price) { return price; }
}

class FleetDiscount implements DiscountStrategy {
    @Override public double calculate(double price) { return price * 0.8; } // 20% flottakedvezmény
}

// 5. Perzisztencia (Persistence with try-with-resources)
class FileManager {
    private static final String FILE_NAME = "bookings.txt";

    public static void save(List<Booking> bookings) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Booking b : bookings) {
                writer.write(b.getLicensePlate() + ";" + b.getServiceName() + ";" + b.getPrice() + ";" + b.getTimeSlot());
                writer.newLine();
            }
        }
    }

    public static List<Booking> load() throws IOException {
        List<Booking> loaded = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return loaded;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length == 4) {
                    loaded.add(new Booking(p[0], p[1], Double.parseDouble(p[2]), p[3]));
                }
            }
        }
        return loaded;
    }
}

// 7. Főprogram és Menü (Main & CLI)
public class Main {
    // 3. Generikus kollekció
    private static List<Booking> bookings = new ArrayList<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- AUTÓMOSÓ FOGLALÁSI RENDSZER ---");
            System.out.println("1. Új foglalás | 2. Listázás | 3. Mentés | 4. Betöltés | 5. Kilépés");
            System.out.print("Választás: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    try {
                        addBooking(sc);
                    } catch (DuplicateBookingException e) {
                        System.out.println("HIBA: " + e.getMessage());
                    }
                    break;
                case "2":
                    listBookings();
                    break;
                case "3":
                    try {
                        FileManager.save(bookings);
                        System.out.println("Sikeres mentés (bookings.txt).");
                    } catch (IOException e) {
                        System.out.println("Mentési hiba: " + e.getMessage());
                    }
                    break;
                case "4":
                    try {
                        bookings = FileManager.load();
                        System.out.println("Sikeres betöltés. (" + bookings.size() + " db foglalás)");
                    } catch (IOException e) {
                        System.out.println("Betöltési hiba: " + e.getMessage());
                    }
                    break;
                case "5":
                    System.out.println("Kilépés...");
                    return;
                default:
                    System.out.println("Érvénytelen opció!");
            }
        }
    }

    private static void addBooking(Scanner sc) throws DuplicateBookingException {
        System.out.print("Időpont (pl. 2026-10-20 14:00): ");
        String time = sc.nextLine().trim();

        // 4. Ütközésvizsgálat
        for (Booking b : bookings) {
            if (b.getTimeSlot().equalsIgnoreCase(time)) {
                throw new DuplicateBookingException("Erre az időpontra már van rögzítve foglalás: " + time);
            }
        }

        System.out.print("Rendszám: ");
        String plate = sc.nextLine().trim().toUpperCase();

        System.out.print("Mosás típusa (1 - Alap 3000 Ft, 2 - Prémium 6000 Ft): ");
        WashType wash = sc.nextLine().trim().equals("2") ? new PremiumWash() : new BasicWash();

        System.out.print("Céges flotta ügyfél? (i/n): ");
        DiscountStrategy discount = sc.nextLine().trim().equalsIgnoreCase("i") ? new FleetDiscount() : new NoDiscount();

        double finalPrice = discount.calculate(wash.getBasePrice());
        bookings.add(new Booking(plate, wash.getName(), finalPrice, time));
        System.out.println("Foglalás rögzítve! Fizetendő: " + finalPrice + " Ft");
    }

    private static void listBookings() {
        if (bookings.isEmpty()) {
            System.out.println("Nincsenek foglalások.");
            return;
        }
        // 3. Rendezés lambda kifejezéssel időpont szerint
        bookings.sort((b1, b2) -> b1.getTimeSlot().compareTo(b2.getTimeSlot()));
        System.out.println("\n--- FOGLALÁSOK LISTÁJA ---");
        bookings.forEach(System.out::println);
    }
}