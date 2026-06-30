package com.github.mcp.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/mcp")
@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/ask")
    public ResponseEntity<String> askAi(@RequestParam String prompt) {
        try {
            log.info("Received prompt: {}", prompt);
            String response = this.chatClient.prompt(prompt)
                    .call()
                    .content();
            log.info("Generated response successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing prompt: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("MCP Client is running on port 8083");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidPrompt(IllegalArgumentException e) {
        log.warn("Invalid prompt: {}", e.getMessage());
        return ResponseEntity.badRequest().body("Invalid prompt: " + e.getMessage());
    }
}
