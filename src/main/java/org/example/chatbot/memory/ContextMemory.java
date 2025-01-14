package org.example.chatbot.memory;
import java.util.HashMap;
import java.util.Map;

public class ContextMemory {
    private final Map<String, String> contextMemory = new HashMap<>();

    public void updateContext(String sessionId, String key, String value) {
        contextMemory.put(sessionId + ":" + key, value);
    }

    public String getContext(String sessionId, String key) {
        return contextMemory.getOrDefault(sessionId + ":" + key, "");
    }

    public void clearContext(String sessionId) {
        contextMemory.entrySet().removeIf(entry -> entry.getKey().startsWith(sessionId + ":"));
    }
}
