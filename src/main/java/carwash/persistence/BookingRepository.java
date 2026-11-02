package carwash.persistence;

import carwash.exception.ValidationException;
import carwash.model.BasicWash;
import carwash.model.Booking;
import carwash.model.Customer;
import carwash.model.PremiumWash;
import carwash.model.WashType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookingRepository {
    private final String filePath;

    public BookingRepository(String filePath) {
        this.filePath = filePath;
    }

    public void save(List<Booking> bookings) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Booking b : bookings) {
                // Mentési formátum: Időpont;Ügyfélnév;Rendszám;MosásTípus;Ár
                writer.write(String.format("%s;%s;%s;%s;%.0f",
                        b.getTimeSlot(),
                        b.getCustomer().getName(),
                        b.getLicensePlate(),
                        b.getWashType().getName(),
                        b.getPrice()));
                writer.newLine();
            }
        }
    }

    public List<Booking> load() throws IOException, ValidationException {
        List<Booking> loaded = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return loaded;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length == 5) {
                    String timeSlot = parts[0];
                    Customer customer = new Customer(parts[1]);
                    String plate = parts[2];
                    WashType washType = parts[3].equalsIgnoreCase("Prémium mosás")
                            ? new PremiumWash()
                            : new BasicWash();
                    double price = Double.parseDouble(parts[4]);

                    loaded.add(new Booking(customer, plate, washType, price, timeSlot));
                }
            }
        }
        return loaded;
    }
}
