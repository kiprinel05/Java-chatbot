package org.example.chatbot;

import org.example.data.DataLoader;
import org.example.chatbot.intentions.IntentRecognizer;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {
    private Map<String, List<String>> conversationHistory;
    private IntentRecognizer intentRecognizer;
    private static final String HISTORY_FILE = "src/main/resources/conversation_history.txt";

    public ChatbotController() throws IOException {
        conversationHistory = new HashMap<>();
        DataLoader.loadFestivalData();
        intentRecognizer = new IntentRecognizer();
        loadConversationHistory();  // ✅ Încarcă istoricul conversației
    }

    /**
     * ✅ Salvează conversația într-un fișier la închiderea aplicației
     */
    private void saveConversationHistory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
            for (Map.Entry<String, List<String>> entry : conversationHistory.entrySet()) {
                writer.write("Session: " + entry.getKey() + "\n");
                for (String message : entry.getValue()) {
                    writer.write(message + "\n");
                }
                writer.write("----\n");
            }
            System.out.println("Conversation history saved successfully!");
        } catch (IOException e) {
            System.err.println("Error saving conversation history: " + e.getMessage());
        }
    }

    /**
     * ✅ Încarcă conversația din fișier la repornirea aplicației
     */
    private void loadConversationHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
            String line;
            String currentSession = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Session: ")) {
                    currentSession = line.replace("Session: ", "").trim();
                    conversationHistory.put(currentSession, new ArrayList<>());
                } else if (line.equals("----")) {
                    currentSession = null;
                } else if (currentSession != null) {
                    conversationHistory.get(currentSession).add(line);
                }
            }
            System.out.println("Conversation history loaded successfully!");
        } catch (IOException e) {
            System.err.println("Error loading conversation history: " + e.getMessage());
        }
    }

    private String getResponse(String input, String sessionId) {
        conversationHistory.putIfAbsent(sessionId, new ArrayList<>());
        conversationHistory.get(sessionId).add("User: " + input);

        // ✅ Procesarea intențiilor prin sinonime
        String normalizedInput = IntentRecognizer.removeDiacritics(input);
        String detectedIntent = intentRecognizer.identifyIntent(normalizedInput);
        String ticketType = intentRecognizer.extractTicketType(normalizedInput);

        switch (detectedIntent) {
            case "INQUIRE_TICKETS":
                int available = DataLoader.getAvailableTickets(ticketType);
                return sendResponse("Avem " + available + " bilete " + ticketType + " disponibile.", sessionId);

            case "INQUIRE_PRICE":
                double price = DataLoader.getTicketPrice(ticketType);
                return sendResponse(price > 0 ?
                        "Prețul pentru un bilet " + ticketType + " este de " + price + " RON."
                        : "Nu am găsit prețul pentru acest tip de bilet.", sessionId);

            case "INQUIRE_RESERVATION":
                int quantity = extractTicketQuantity(normalizedInput);
                boolean reserved = DataLoader.reserveTickets(ticketType, quantity);
                return reserved ?
                        sendResponse(quantity + " bilete " + ticketType + " rezervate cu succes.", sessionId)
                        : sendResponse("Nu avem suficiente bilete disponibile.", sessionId);

            case "INQUIRE_ARTIST":
                // ✅ Extraxt specific pentru artist în loc de ticketType
                String artistName = normalizedInput.replaceAll("(cand canta|cand este|when is|performanta)", "").trim();
                String artistInfo = DataLoader.getArtistPerformance(artistName);
                return sendResponse(
                        artistInfo.contains("nu a fost găsit")
                                ? "Nu am găsit informații despre artistul menționat. Te rog să verifici numele."
                                : capitalizeFirstLetter(artistName) + " va cânta în data de " + artistInfo,
                        sessionId
                );
            case "LIST_ARTISTS":
                List<String> allArtists = DataLoader.getArtistsByDate("");
                return sendResponse("Artiștii programați sunt: " + String.join(", ", allArtists), sessionId);

            // ✅ Implementarea resetării sesiunii
            case "RESET_SESSION":
                conversationHistory.remove(sessionId);
                saveConversationHistory();
                return sendResponse("Sesiunea a fost resetată cu succes.", sessionId);

            default:
                return sendResponse("Încă nu înțeleg această întrebare. Încearcă să întrebi despre bilete, prețuri sau artiști.", sessionId);
        }
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private int extractTicketQuantity(String input) {
        String[] words = input.split(" ");
        for (String word : words) {
            try {
                return Integer.parseInt(word);
            } catch (NumberFormatException ignored) {
            }
        }
        return 1;
    }

    private String sendResponse(String response, String sessionId) {
        response = capitalizeFirstLetter(response);
        conversationHistory.get(sessionId).add("Bot: " + response);
        saveConversationHistory();  // ✅ Salvăm conversația după fiecare răspuns
        return response;
    }

    @PostMapping("/send")
    public Map<String, String> sendMessage(@RequestBody Map<String, String> request) {
        String userInput = request.get("message");
        String sessionId = request.getOrDefault("sessionId", UUID.randomUUID().toString());

        String response = getResponse(userInput, sessionId);

        Map<String, String> jsonResponse = new HashMap<>();
        jsonResponse.put("response", response);
        jsonResponse.put("sessionId", sessionId);
        return jsonResponse;
    }

    @GetMapping("/history/{sessionId}")
    public Map<String, List<String>> getConversationHistory(@PathVariable String sessionId) {
        Map<String, List<String>> historyResponse = new HashMap<>();

        // ✅ Verificare dacă sesiunea există
        if (!conversationHistory.containsKey(sessionId)) {
            historyResponse.put("history", List.of("No active session found."));
            return historyResponse;
        }

        historyResponse.put("history", conversationHistory.getOrDefault(sessionId, new ArrayList<>()));
        return historyResponse;
    }
}