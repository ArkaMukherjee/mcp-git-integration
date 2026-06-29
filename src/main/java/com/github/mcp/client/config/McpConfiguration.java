package com.github.mcp.client.config;

import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class McpConfiguration {

    private static final Logger log = LoggerFactory.getLogger(McpConfiguration.class);
    private final ToolCallbackProvider toolCallbackProvider;

    public McpConfiguration(ToolCallbackProvider toolCallbackProvider, McpSyncClient mcpSyncClient) {
        this.toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpSyncClient);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultTools(toolCallbackProvider)
                .build();
    }

//    @Bean
//    public CommandLineRunner runRunner(ChatClient chatClient) {
//        return args -> {
//            System.out.println("--- Prompting Claude using GitHub MCP Server Tools ---");
//
//            // Claude will analyze the intent, invoke the GitHub tool automatically, and present the text result
//            String answer = chatClient.prompt()
//                    .user("Search for recent repository issues matching 'bug' in owner 'spring-projects' repo 'spring-ai'")
//                    .call()
//                    .content();
//
//            System.out.println("Claude Response:\n" + answer);
//        };
//    }

    // Log discovered tools at startup
    @PostConstruct
    public void logAvailableTools() {
        ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
        log.info("=== MCP Tools discovered from GitHub server: {} ===", tools.length);
        Arrays.stream(tools).forEach(t ->
                log.info("  → [{}] : {}",
                        t.getToolDefinition().name(),
                        t.getToolDefinition().description())
        );
    }
}
