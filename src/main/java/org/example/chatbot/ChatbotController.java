package org.example.chatbot;

import org.example.chatbot.intentions.IntentRecognizer;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.data.Interfaces.ArtistRepository;
import org.example.data.Interfaces.TicketRepository;
import org.example.data.Artist;
import org.example.data.Ticket;
import org.example.chatbot.transport.TransportController;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    private final ArtistRepository artistRepository;
    private final TicketRepository ticketRepository;
    private final IntentRecognizer intentRecognizer;
    private final Map<String, List<String>> conversationHistory = new HashMap<>();
    private final Map<String, String> lastTicketType = new HashMap<>();
    private final Map<String, String> lastArtist = new HashMap<>();
    private final Map<String, String> lastMessage = new HashMap<>();
    private final Map<String, String> lastIntent = new HashMap<>();
    private TransportController transportController;
    @Autowired
    public ChatbotController(ArtistRepository artistRepository, TicketRepository ticketRepository, TransportController transportController) {
        this.artistRepository = artistRepository;
        this.ticketRepository = ticketRepository;
        this.intentRecognizer = new IntentRecognizer();
        this.transportController = transportController;
    }


    private String getResponse(String input, String sessionId) {
        conversationHistory.putIfAbsent(sessionId, new ArrayList<>());
        conversationHistory.get(sessionId).add("User: " + input);

        String normalizedInput = IntentRecognizer.removeDiacritics(input).toLowerCase().replace("?", "").trim();
        String detectedIntent = intentRecognizer.identifyIntent(normalizedInput);
        String ticketType = intentRecognizer.extractTicketType(normalizedInput);

        // Reținerea ultimului mesaj și intenție
        String previousMessage = lastMessage.getOrDefault(sessionId, "");
        String previousIntent = lastIntent.getOrDefault(sessionId, "UNKNOWN");
        lastMessage.put(sessionId, input);

        // Gestionare întrebări de tip "dar"
        if (normalizedInput.matches(".*dar.*")) {
            if (previousIntent.equals("INQUIRE_ARTIST")) {
                detectedIntent = "INQUIRE_ARTIST";
            } else if (previousIntent.equals("INQUIRE_PRICE")) {
                detectedIntent = "INQUIRE_PRICE";
                ticketType = lastTicketType.getOrDefault(sessionId, null);
            }
        }

        // Actualizare context pe baza tipului detectat
        if (ticketType != null) {
            lastTicketType.put(sessionId, ticketType);
        }
        lastIntent.put(sessionId, detectedIntent);

        switch (detectedIntent) {

            case "INQUIRE_TICKETS":
                return getTicketAvailability(ticketType, sessionId);

            case "INQUIRE_PRICE":
                return getTicketPrice(ticketType, sessionId);

            case "INQUIRE_RESERVATION":
                int quantity = extractTicketQuantity(normalizedInput);
                return reserveTickets(ticketType, quantity, sessionId);

            case "INQUIRE_ARTIST":
                String artistName = normalizedInput.replaceAll("(cand canta|cand este|when is|performanta|dar)", "").trim();
                if (artistName.isEmpty() && lastArtist.containsKey(sessionId)) {
                    artistName = lastArtist.get(sessionId);
                } else {
                    lastArtist.put(sessionId, artistName);
                }
                return getArtistPerformance(artistName, sessionId);

            case "LIST_ARTISTS":
                return listAllArtists(sessionId);

            case "RESET_SESSION":
                conversationHistory.remove(sessionId);
                lastTicketType.remove(sessionId);
                lastArtist.remove(sessionId);
                lastMessage.remove(sessionId);
                lastIntent.remove(sessionId);
                return sendResponse("Sesiunea a fost resetată cu succes.", sessionId);

                case "TRANSPORT":
                // Logica pentru cererile de transport
                Map<String, String> transportRequest = Map.of("message", input);
                Map<String, String> transportResponse = transportController.getTransport(transportRequest);
                return sendResponse(transportResponse.get("response"), sessionId);
            default:
                return sendResponse("Încă nu înțeleg această întrebare. Încearcă să întrebi despre bilete, prețuri sau artiști.", sessionId);
        }
    }

    private String getTicketAvailability(String ticketType, String sessionId) {
        Optional<Ticket> ticket = ticketRepository.findById(ticketType);
        if (ticket.isPresent()) {
            return sendResponse("Avem " + ticket.get().getAvailable() + " bilete " + ticketType + " disponibile.", sessionId);
        }
        return sendResponse("Nu am găsit bilete de tipul specificat.", sessionId);
    }

    private String getTicketPrice(String ticketType, String sessionId) {
        Optional<Ticket> ticket = ticketRepository.findById(ticketType);
        if (ticket.isPresent()) {
            lastTicketType.put(sessionId, ticketType); // Actualizare context
            return sendResponse("Prețul pentru un bilet " + ticketType + " este de " + ticket.get().getPrice() + " RON.", sessionId);
        }
        return sendResponse("Nu am găsit prețul pentru acest tip de bilet.", sessionId);
    }

    private String reserveTickets(String ticketType, int quantity, String sessionId) {
        if (ticketType == null || ticketType.isEmpty()) {
            return sendResponse("Te rog să specifici tipul de bilet pentru rezervare.", sessionId);
        }
        Optional<Ticket> ticket = ticketRepository.findById(ticketType);
        if (ticket.isPresent() && ticket.get().getAvailable() >= quantity) {
            Ticket updatedTicket = new Ticket(ticketType, ticket.get().getAvailable() - quantity, ticket.get().getPrice());
            ticketRepository.save(updatedTicket);
            return sendResponse(quantity + " bilete " + ticketType + " rezervate cu succes.", sessionId);
        }
        return sendResponse("Ne pare rău, nu avem suficiente bilete disponibile.", sessionId);
    }

    private String getArtistPerformance(String artistName, String sessionId) {
        if (artistName == null || artistName.isEmpty()) {
            return sendResponse("Te rog să specifici numele artistului.", sessionId);
        }

        String normalizedArtistName = IntentRecognizer.removeDiacritics(artistName).toLowerCase().replaceAll("\\s+", "").trim();

        Optional<Artist> artist = artistRepository.findAll().stream()
                .filter(a -> IntentRecognizer.removeDiacritics(a.getName()).toLowerCase().replaceAll("\\s+", "").trim().equals(normalizedArtistName))
                .findFirst();

        if (artist.isPresent()) {
            lastArtist.put(sessionId, artist.get().getName()); // Actualizare context
            return sendResponse(artist.get().getName() + " va cânta în data de " + artist.get().getPerformanceDate(), sessionId);
        }
        return sendResponse("Nu am găsit informații despre acest artist.", sessionId);
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

    private String listAllArtists(String sessionId) {
        List<Artist> allArtists = artistRepository.findAll();
        if (allArtists.isEmpty()) {
            return sendResponse("Nu există artiști programați.", sessionId);
        }
        List<String> artistNames = new ArrayList<>();
        for (Artist artist : allArtists) {
            artistNames.add(artist.getName());
        }
        return sendResponse("Artiștii programați sunt: " + String.join(", ", artistNames), sessionId);
    }

    private String sendResponse(String response, String sessionId) {
        conversationHistory.get(sessionId).add("Bot: " + response);
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
        if (!conversationHistory.containsKey(sessionId)) {
            historyResponse.put("history", List.of("No active session found."));
        } else {
            historyResponse.put("history", conversationHistory.get(sessionId));
        }
        return historyResponse;
    }
}
