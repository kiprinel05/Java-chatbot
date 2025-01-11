package org.example.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;

public class DataLoader {
    private static final String ARTISTS_FILE = "src/main/resources/artists.in";
    private static final String TICKETS_FILE = "src/main/resources/tickets.in";

    private static final Map<String, String> artistSchedule = new HashMap<>();
    private static final Map<String, Integer> ticketAvailability = new HashMap<>();
    private static final Map<String, Double> ticketPrices = new HashMap<>();

    /**
     * ✅ Eliminarea diacriticelor și normalizarea textului
     */
    private static String removeDiacritics(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim();
    }

    /**
     * ✅ Metodă de inițializare pentru a încărca datele
     */
    public static void loadFestivalData() throws IOException {
        loadArtistsData();
        loadTicketsData();
    }

    /**
     * ✅ Citirea datelor despre artiști din fișier cu eliminarea diacriticelor
     */
    private static void loadArtistsData() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(ARTISTS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length == 3) {
                    String artistName = removeDiacritics(parts[0]);
                    String date = parts[1];
                    String time = parts[2];
                    artistSchedule.put(artistName, date + " at " + time);
                }
            }
        }
        System.out.println("Artists data loaded successfully!");
    }

    /**
     * ✅ Citirea datelor despre bilete din fișier
     */
    private static void loadTicketsData() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(TICKETS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length == 3) {
                    String ticketType = removeDiacritics(parts[0]);
                    double price = Double.parseDouble(parts[1]);
                    int available = Integer.parseInt(parts[2]);
                    ticketPrices.put(ticketType, price);
                    ticketAvailability.put(ticketType, available);
                }
            }
        }
        System.out.println("Tickets data loaded successfully!");
    }

    /**
     * ✅ Returnarea programului unui artist specific
     */
    public static String getArtistPerformance(String artistName) {
        String normalizedArtist = removeDiacritics(artistName);

        // ✅ Potrivire exactă după eliminarea diacriticelor
        if (artistSchedule.containsKey(normalizedArtist)) {
            return artistSchedule.get(normalizedArtist);
        }

        // ✅ Potrivire parțială (similaritate) pentru cazuri mai flexibile
        for (Map.Entry<String, String> entry : artistSchedule.entrySet()) {
            if (removeDiacritics(entry.getKey()).contains(normalizedArtist)) {
                return entry.getValue();
            }
        }

        return "Artistul nu a fost găsit.";
    }

    /**
     * ✅ Returnarea numărului de bilete disponibile
     */
    public static int getAvailableTickets(String ticketType) {
        String normalizedTicketType = removeDiacritics(ticketType);
        return ticketAvailability.getOrDefault(normalizedTicketType, 0);
    }

    /**
     * ✅ Returnarea prețului unui bilet
     */
    public static double getTicketPrice(String ticketType) {
        String normalizedTicketType = removeDiacritics(ticketType);
        return ticketPrices.getOrDefault(normalizedTicketType, -1.0);
    }

    /**
     * ✅ Rezervarea biletelor în timp real
     */
    public static boolean reserveTickets(String ticketType, int quantity) {
        String normalizedTicketType = removeDiacritics(ticketType);
        int available = getAvailableTickets(normalizedTicketType);
        if (available >= quantity) {
            ticketAvailability.put(normalizedTicketType, available - quantity);
            return true;
        }
        return false;
    }

    /**
     * ✅ Căutare după dată - Returnează artiștii care cântă într-o anumită zi
     */
    public static List<String> getArtistsByDate(String date) {
        List<String> artistsOnDate = new ArrayList<>();
        for (Map.Entry<String, String> entry : artistSchedule.entrySet()) {
            if (entry.getValue().contains(date)) {
                artistsOnDate.add(entry.getKey());
            }
        }
        return artistsOnDate.isEmpty() ? List.of("Niciun artist programat în această zi.") : artistsOnDate;
    }
}
