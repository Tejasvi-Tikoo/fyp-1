package com.wildroutes.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Value("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")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String message) {

        String url = "https://openrouter.ai/api/v1/chat/completions";

        // 🧠 Request body
        Map<String, Object> body = new HashMap<>();
        body.put("model", "openai/gpt-3.5-turbo");

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", message);

        messages.add(userMsg);

        body.put("messages", messages);

        // 📡 Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            List choices = (List) response.getBody().get("choices");
            Map first = (Map) choices.get(0);
            Map msg = (Map) first.get("message");

            return msg.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}