package org.example.chatbot.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalTime;
import java.util.*;
@RestController
@RequestMapping("/api/transport")
public class TransportController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TransportController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/search")
    public Map<String, String> getTransport(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("message", "").toLowerCase();

        String departure = extractLocation(userInput, "din");
        String destination = extractLocation(userInput, "in");

        if (destination == null || destination.isEmpty()) {
            destination = "Costinesti";
        }

        if (departure == null || departure.isEmpty()) {
            return Map.of("response", "Te rog să specifici locația de plecare.");
        }

        String sql = "SELECT schedule, route FROM transport WHERE departure = ? AND ? = ANY(route)";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, departure, destination);

        if (results.isEmpty()) {
            return Map.of("response", "Nu am găsit transport disponibil din " + departure + " către " + destination + ".");
        }

        Map<String, Object> nextTransport = findNextTransport(results);
        String schedule = (String) nextTransport.get("schedule");
        List<String> route = (List<String>) nextTransport.get("route");

        String response = "Următorul transport din " + departure + " către " + destination + " pleacă la ora " + schedule +
                ". Traseul este: " + String.join(" -> ", route) + ".";
        return Map.of("response", response);
    }

    private String extractLocation(String input, String keyword) {
        int index = input.indexOf(keyword);
        if (index == -1) return null;

        String[] words = input.substring(index + keyword.length()).trim().split(" ");
        return words.length > 0 ? words[0] : null;
    }

    private Map<String, Object> findNextTransport(List<Map<String, Object>> results) {
        LocalTime now = LocalTime.now();

        for (Map<String, Object> result : results) {
            String[] scheduleArray = (String[]) result.get("schedule");
            for (String time : scheduleArray) {
                LocalTime busTime = LocalTime.parse(time);
                if (busTime.isAfter(now)) {
                    result.put("schedule", time);
                    return result;
                }
            }
        }

        return results.get(0);
    }
}
