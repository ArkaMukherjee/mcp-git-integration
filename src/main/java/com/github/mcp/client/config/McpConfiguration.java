package com.github.mcp.client.config;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class McpConfiguration {

    private static final Logger log = LoggerFactory.getLogger(McpConfiguration.class);
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * Create ToolCallbackProvider from the auto-discovered McpSyncClient instances.
     * Uses ObjectProvider for graceful fallback when mcpSyncClients is not available.
     * This prevents the entire bean chain from failing if MCP initialization fails.
     */
    @Bean
    public ToolCallbackProvider toolCallbackProvider(ObjectProvider<List<McpSyncClient>> mcpSyncClientsProvider) {
        try {
            List<McpSyncClient> mcpSyncClients = mcpSyncClientsProvider.getIfAvailable();

            if (mcpSyncClients == null || mcpSyncClients.isEmpty()) {
                log.warn("No MCP Sync Clients available. MCP tools will not be available. "
                        + "Check that the GitHub MCP server is running and properly configured.");
                // Return empty provider - allows app to start without MCP tools
                this.toolCallbackProvider = () -> new ToolCallback[0];
                return this.toolCallbackProvider;
            }

            // Use the first (and typically only) MCP client for tool callbacks
            McpSyncClient mcpClient = mcpSyncClients.getFirst();
            log.debug("Creating ToolCallbackProvider from McpSyncClient: {}", mcpClient);

            // Create the tool callback provider using the proper Spring AI 2.0 constructor
            this.toolCallbackProvider = new SyncMcpToolCallbackProvider(mcpClient);
            log.info("ToolCallbackProvider successfully created with MCP client");
            return this.toolCallbackProvider;
        } catch (Exception e) {
            log.error("Failed to create ToolCallbackProvider: {}. Continuing without MCP tools.",
                    e.getMessage(), e);
            // Fallback: return empty provider so app can start without MCP tools
            this.toolCallbackProvider = () -> new ToolCallback[0];
            return this.toolCallbackProvider;
        }
    }

    /**
     * Create ChatClient bean with MCP tools integrated.
     * The ChatClient will automatically use MCP tools when processing prompts.
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultTools(toolCallbackProvider)
                .build();
    }

    /**
     * Log all discovered tools after the application is ready. Using ApplicationReadyEvent
     * ensures this runs after Spring has initialized other beans (including the
     * `toolCallbackProvider` bean) so it won't run too early.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (toolCallbackProvider == null) {
            log.warn("Tool callback provider is null, waiting for initialization...");
            return;
        }

        try {
            ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
            log.info("=== MCP Tools discovered from GitHub server: {} ===", tools.length);

            if (tools.length == 0) {
                log.warn("No MCP tools discovered. Check GitHub MCP server configuration and environment variables.");
            } else {
                Arrays.stream(tools).forEach(t ->
                        log.info("  → [{}] : {}",
                                t.getToolDefinition().name(),
                                t.getToolDefinition().description())
                );
            }
        } catch (Exception e) {
            log.error("Error retrieving tool callbacks: {}", e.getMessage(), e);
        }
    }
}
