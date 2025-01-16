package org.example.chatbot.intentions;

import java.text.Normalizer;
import java.util.*;

public class IntentRecognizer {

    private final Map<String, List<String>> synonyms;

    public IntentRecognizer() {
        synonyms = new HashMap<>();
        synonyms.put("bilete", Arrays.asList("bilete", "tichete", "locuri", "intrari", "bilet"));
        synonyms.put("pret", Arrays.asList("pret", "costa", "tarif", "cost", "valoare", "cat"));
        synonyms.put("artist", Arrays.asList("artist", "formatie", "trupa", "cantaret", "band", "canta", "performanta"));
        synonyms.put("list_artists", Arrays.asList("ce artisti vor fi prezenti", "ce artisti vor canta",
                "lista artistilor", "da-mi o lista de artisti",
                "spune-mi artistii disponibili", "lista artisti", "ce artisti vor performa"));
        synonyms.put("transport", Arrays.asList("transport", "autobuz", "microbuz", "calatorie", "drum", "plecare"));
    }

    public static String removeDiacritics(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim();
    }

    private boolean containsSynonym(String input, String key) {
        List<String> synonymsList = synonyms.getOrDefault(key, Collections.emptyList());
        String normalizedInput = removeDiacritics(input);
        return synonymsList.stream().anyMatch(normalizedInput::contains);
    }

    /**
     * ✅ Identificarea intenției bazată pe sinonime
     */
    public String identifyIntent(String input) {
        String normalizedInput = removeDiacritics(input).toLowerCase().trim();

        if (containsSynonym(normalizedInput, "bilete") && normalizedInput.matches(".*(cate|ramase|disponibile|mai sunt).*")) {
            return "INQUIRE_TICKETS";
        }
        if (containsSynonym(normalizedInput, "pret") && containsSynonym(normalizedInput, "bilete")) {
            return "INQUIRE_PRICE";
        }
        if (normalizedInput.matches(".*(vreau sa rezerv|as dori sa rezerv|vreau bilete).*")) {
            return "INQUIRE_RESERVATION";
        }
        if (containsSynonym(normalizedInput, "artist") && normalizedInput.matches(".*(cand canta|cand este|when is|performanta).*")) {
            return "INQUIRE_ARTIST";
        }
        if (containsSynonym(normalizedInput, "list_artists")) {
            return "LIST_ARTISTS";
        }
        if (normalizedInput.equals("reset")) {
            return "RESET_SESSION";
        }
        if (containsSynonym(normalizedInput, "transport") && normalizedInput.contains("din")) {
            return "TRANSPORT";
        }
        return "UNKNOWN_INTENT";
    }

    public String extractTicketType(String input) {
        String normalizedInput = removeDiacritics(input).toLowerCase();
        if (normalizedInput.contains("ultra vip")) return "ultra vip";
        if (normalizedInput.contains("vip")) return "vip";
        if (normalizedInput.contains("general admission") || normalizedInput.contains("general")) return "general admission";
        return "general admission";  // ✅ Implicit
    }
}
