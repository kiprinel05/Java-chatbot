package org.example.chatbot;

import org.example.chatbot.intentions.IntentRecognizer;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.data.ArtistRepository;
import org.example.data.TicketRepository;
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

    @Autowired
    public ChatbotController(ArtistRepository artistRepository, TicketRepository ticketRepository) {
        this.artistRepository = artistRepository;
        this.ticketRepository = ticketRepository;
        this.intentRecognizer = new IntentRecognizer();
    }

    /**
     * ✅ Returnează un răspuns bazat pe intenția detectată
     */
    private String getResponse(String input, String sessionId) {
        conversationHistory.putIfAbsent(sessionId, new ArrayList<>());
        conversationHistory.get(sessionId).add("User: " + input);

        // ✅ Normalizarea și identificarea intenției
        String normalizedInput = IntentRecognizer.removeDiacritics(input).toLowerCase().trim();
        String detectedIntent = intentRecognizer.identifyIntent(normalizedInput);
        String ticketType = intentRecognizer.extractTicketType(normalizedInput);

        switch (detectedIntent) {

            // ✅ Verificarea biletelor disponibile
            case "INQUIRE_TICKETS":
                List<Ticket> allTickets = ticketRepository.findAll();
                Optional<Ticket> matchingTicketType = allTickets.stream()
                        .filter(t -> t.getType().equalsIgnoreCase(ticketType))
                        .findFirst();

                if (matchingTicketType.isPresent()) {
                    return sendResponse("Avem " + matchingTicketType.get().getAvailable()
                            + " bilete de tip " + matchingTicketType.get().getType() + " disponibile.", sessionId);
                } else {
                    return sendResponse("Nu am găsit bilete de tipul specificat.", sessionId);
                }

                // ✅ Returnarea prețului pentru un tip de bilet
            case "INQUIRE_PRICE":
                Optional<Ticket> ticketForPrice = ticketRepository.findAll().stream()
                        .filter(t -> t.getType().equalsIgnoreCase(ticketType))
                        .findFirst();

                if (ticketForPrice.isPresent()) {
                    return sendResponse("Prețul pentru un bilet de tip " + ticketType
                            + " este de " + ticketForPrice.get().getPrice() + " RON.", sessionId);
                } else {
                    return sendResponse("Nu am găsit prețul pentru acest tip de bilet.", sessionId);
                }

                // ✅ Rezervarea biletelor
            case "INQUIRE_RESERVATION":
                int quantity = extractTicketQuantity(normalizedInput);
                Optional<Ticket> ticketToReserve = ticketRepository.findAll().stream()
                        .filter(t -> t.getType().equalsIgnoreCase(ticketType))
                        .findFirst();

                if (ticketToReserve.isPresent() && ticketToReserve.get().getAvailable() >= quantity) {
                    Ticket updatedTicket = new Ticket(ticketType, ticketToReserve.get().getAvailable() - quantity, ticketToReserve.get().getPrice());
                    ticketRepository.save(updatedTicket);
                    return sendResponse(quantity + " bilete " + ticketType + " rezervate cu succes.", sessionId);
                } else {
                    return sendResponse("Nu există suficiente bilete disponibile pentru rezervare.", sessionId);
                }

                // ✅ Căutarea unui artist
            case "INQUIRE_ARTIST":
                String artistName = normalizedInput.replaceAll("(cand canta|cand este|when is|performanta)", "").trim();
                List<Artist> allArtists = artistRepository.findAll();
                Optional<Artist> matchingArtist = allArtists.stream()
                        .filter(a -> a.getName().equalsIgnoreCase(artistName))
                        .findFirst();

                if (matchingArtist.isPresent()) {
                    return sendResponse(matchingArtist.get().getName() + " va cânta în data de "
                            + matchingArtist.get().getPerformanceDate() + " la ora "
                            + matchingArtist.get().getPerformanceTime() + ".", sessionId);
                } else {
                    return sendResponse("Nu am găsit informații despre acest artist.", sessionId);
                }
                // ✅ Listarea tuturor artiștilor
            case "LIST_ARTISTS":
                List<Artist> artistList = artistRepository.findAll();
                if (artistList.isEmpty()) {
                    return sendResponse("Nu există artiști disponibili.", sessionId);
                } else {
                    String artistNames = artistList.stream()
                            .map(Artist::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    return sendResponse("Artiștii disponibili sunt: " + artistNames, sessionId);
                }

                // ✅ Resetarea sesiunii
            case "RESET_SESSION":
                conversationHistory.remove(sessionId);
                return sendResponse("Sesiunea a fost resetată cu succes.", sessionId);

            // ✅ Intenție necunoscută
            default:
                return sendResponse("Încă nu înțeleg această întrebare. Încearcă să întrebi despre bilete, prețuri sau artiști.", sessionId);
        }
    }

    /**
     * ✅ Returnează numărul de bilete disponibile
     */
    private String getTicketAvailability(String ticketType, String sessionId) {
        Optional<Ticket> ticket = ticketRepository.findById(ticketType);
        if (ticket.isPresent()) {
            return sendResponse("Avem " + ticket.get().getAvailable() + " bilete " + ticketType + " disponibile.", sessionId);
        }
        return sendResponse("Nu am găsit bilete de tipul specificat.", sessionId);
    }

    /**
     * ✅ Returnează prețul unui bilet
     */
    private String getTicketPrice(String ticketType, String sessionId) {
        Optional<Ticket> ticket = ticketRepository.findById(ticketType);
        if (ticket.isPresent()) {
            return sendResponse("Prețul pentru un bilet " + ticketType + " este de " + ticket.get().getPrice() + " RON.", sessionId);
        }
        return sendResponse("Nu am găsit prețul pentru acest tip de bilet.", sessionId);
    }

    /**
     * ✅ Rezervarea biletelor
     */
    private String reserveTickets(String ticketType, int quantity, String sessionId) {
        Optional<Ticket> ticket = ticketRepository.findById(ticketType);
        if (ticket.isPresent() && ticket.get().getAvailable() >= quantity) {
            Ticket updatedTicket = new Ticket(ticketType, ticket.get().getAvailable() - quantity, ticket.get().getPrice());
            ticketRepository.save(updatedTicket);
            return sendResponse(quantity + " bilete " + ticketType + " rezervate cu succes.", sessionId);
        }
        return sendResponse("Ne pare rău, nu avem suficiente bilete disponibile.", sessionId);
    }

    /**
     * ✅ Returnează programul unui artist
     */
    private String getArtistPerformance(String artistName, String sessionId) {
        Optional<Artist> artist = artistRepository.findById(artistName);
        if (artist.isPresent()) {
            return sendResponse(artist.get().getName() + " va cânta în data de " + artist.get().getPerformanceDate(), sessionId);
        }
        return sendResponse("Nu am găsit informații despre acest artist.", sessionId);
    }

    /**
     * ✅ Listarea tuturor artiștilor
     */
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

    /**
     * ✅ Extrage cantitatea de bilete din input
     */
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

    /**
     * ✅ Trimiterea unui răspuns în chat și salvarea conversației
     */
    private String sendResponse(String response, String sessionId) {
        conversationHistory.get(sessionId).add("Bot: " + response);
        return response;
    }

    /**
     * ✅ Endpoint pentru trimiterea de mesaje către chatbot
     */
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

    /**
     * ✅ Endpoint pentru vizualizarea istoricului conversației
     */
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
