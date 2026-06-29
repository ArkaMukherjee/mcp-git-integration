package com.github.mcp.client.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/mcp")
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient; // Placeholder, will be injected by Spring
    }

    @GetMapping("/ask")
    public String askAi(@RequestParam String prompt) {
        return this.chatClient.prompt(prompt)
                .call()
                .content();
    }
}
