package org.example.chatbot;

import org.example.chatbot.intentions.IntentRecognizer;
import org.example.chatbot.memory.ContextMemory;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.data.interfaces.ArtistRepository;
import org.example.data.interfaces.TicketRepository;
import org.example.data.Artist;
import org.example.data.Ticket;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    private final ArtistRepository artistRepository;
    private final TicketRepository ticketRepository;
    private final IntentRecognizer intentRecognizer;
    private final Map<String, List<String>> conversationHistory = new HashMap<>();
    private final ContextMemory contextMemory = new ContextMemory();
    @Autowired
    public ChatbotController(ArtistRepository artistRepository, TicketRepository ticketRepository) {
        this.artistRepository = artistRepository;
        this.ticketRepository = ticketRepository;
        this.intentRecognizer = new IntentRecognizer();
    }



    private String getResponse(String input, String sessionId) {
        conversationHistory.putIfAbsent(sessionId, new ArrayList<>());
        conversationHistory.get(sessionId).add("User: " + input);

        String normalizedInput = IntentRecognizer.removeDiacritics(input).toLowerCase().trim();

        // Intent recognition based on context first
        String detectedIntent = contextMemory.getContext(sessionId, "lastDetectedIntent");
        if (detectedIntent == null || detectedIntent.isEmpty()) {
            detectedIntent = intentRecognizer.identifyIntent(normalizedInput);
            contextMemory.updateContext(sessionId, "lastDetectedIntent", detectedIntent);
        }

        String ticketType = intentRecognizer.extractTicketType(normalizedInput);
        if (ticketType != null && !ticketType.isEmpty()) {
            contextMemory.updateContext(sessionId, "lastTicketType", ticketType);
        } else {
            ticketType = contextMemory.getContext(sessionId, "lastTicketType");
        }

        String lastArtist = contextMemory.getContext(sessionId, "lastArtistName");

        switch (detectedIntent) {
            case "INQUIRE_TICKETS":
                return getTicketAvailability(ticketType, sessionId);

            case "INQUIRE_PRICE":
                return getTicketPrice(ticketType, sessionId);

            case "INQUIRE_RESERVATION":
                return reserveTickets(ticketType, extractTicketQuantity(normalizedInput), sessionId);

            case "INQUIRE_ARTIST":
                String artistName = normalizedInput.replaceAll("(cand canta|cand este|when is|performanta)", "").trim();
                if (artistName.isEmpty()) {
                    artistName = lastArtist;
                }
                contextMemory.updateContext(sessionId, "lastArtistName", artistName);
                return getArtistPerformance(artistName, sessionId);

            case "LIST_ARTISTS":
                return listAllArtists(sessionId);

            case "RESET_SESSION":
                conversationHistory.remove(sessionId);
                contextMemory.clearContext(sessionId);
                return sendResponse("Sesiunea a fost resetată cu succes.", sessionId);

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
            return sendResponse("Prețul pentru un bilet " + ticketType + " este de " + ticket.get().getPrice() + " RON.", sessionId);
        }
        return sendResponse("Nu am găsit prețul pentru acest tip de bilet.", sessionId);
    }

    private String reserveTickets(String ticketType, int quantity, String sessionId) {
        Optional<Ticket> ticket = ticketRepository.findById(ticketType);
        if (ticket.isPresent() && ticket.get().getAvailable() >= quantity) {
            Ticket updatedTicket = new Ticket(ticketType, ticket.get().getAvailable() - quantity, ticket.get().getPrice());
            ticketRepository.save(updatedTicket);
            return sendResponse(quantity + " bilete " + ticketType + " rezervate cu succes.", sessionId);
        }
        return sendResponse("Ne pare rău, nu avem suficiente bilete disponibile.", sessionId);
    }

    private String getArtistPerformance(String artistName, String sessionId) {
        Optional<Artist> artist = artistRepository.findById(artistName);
        if (artist.isPresent()) {
            return sendResponse(artist.get().getName() + " va cânta în data de " + artist.get().getPerformanceDate(), sessionId);
        }
        return sendResponse("Nu am găsit informații despre acest artist.", sessionId);
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
