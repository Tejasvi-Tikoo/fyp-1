package com.wildroutes.ai;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService geminiService;

    // 🔗 Constructor Injection
    public AIController(AIService geminiService) {
        this.geminiService = geminiService;
    }

    // 💬 Chat API
    @PostMapping("/chat")
    public String chat(@RequestBody Map<String, String> request) {

        // 📨 Get message from frontend/Postman
        String message = request.get("message");

        // 🤖 Send to Gemini
        return geminiService.chat(message);
    }
}