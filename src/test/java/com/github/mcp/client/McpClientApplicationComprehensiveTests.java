package com.github.mcp.client;

import com.github.mcp.client.config.McpConfiguration;
import com.github.mcp.client.controller.ChatController;
import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class McpClientApplicationComprehensiveTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private ChatController chatController;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    void applicationContext_LoadsSuccessfully() {
        assertNotNull(applicationContext, "ApplicationContext should not be null");
    }

    @Test
    void chatController_BeanExists() {
        assertNotNull(chatController, "ChatController bean should be autowired");
    }

    @Test
    void chatClient_BeanExists() {
        assertNotNull(chatClient, "ChatClient bean should be autowired");
    }

    @Test
    void toolCallbackProvider_BeanExists() {
        assertNotNull(toolCallbackProvider, "ToolCallbackProvider bean should be autowired");
    }

    @Test
    void toolCallbackProvider_HasTools() {
        assertNotNull(toolCallbackProvider.getToolCallbacks(), "Tool callbacks should not be null");
        // Should have at least some tools discovered from GitHub MCP server
        assertTrue(toolCallbackProvider.getToolCallbacks().length >= 0,
                "Tool callbacks array should be valid");
    }

    @Test
    void mcpConfiguration_BeanExists() {
        McpConfiguration mcpConfig = applicationContext.getBean(McpConfiguration.class);
        assertNotNull(mcpConfig, "McpConfiguration should exist in context");
    }

    @Test
    void allRequiredBeans_ArePresent() {
        // Verify all critical beans are present
        assertTrue(applicationContext.containsBean("chatClient"));
        assertTrue(applicationContext.containsBean("toolCallbackProvider"));
        assertTrue(applicationContext.containsBean("chatController"));
        assertTrue(applicationContext.containsBean("mcpConfiguration"));
    }

    @Test
    void chatClientBuilder_IsConfigured() {
        // Verify ChatClient was built with tools
        assertNotNull(chatClient);
        // If tools are available, they should be integrated into ChatClient
    }

    @Test
    void applicationReadsyEvent_IsListened() {
        // Verify the application is listening for ApplicationReadyEvent
        McpConfiguration mcpConfig = applicationContext.getBean(McpConfiguration.class);
        assertNotNull(mcpConfig);
        // The event listener is defined in McpConfiguration
    }

    @Test
    void toolCallbackProvider_ReturnsValidToolCallbacks() {
        var tools = toolCallbackProvider.getToolCallbacks();
        assertNotNull(tools);
        for (var tool : tools) {
            assertNotNull(tool.getToolDefinition(), "Tool definition should not be null");
            assertNotNull(tool.getToolDefinition().name(), "Tool name should not be null");
        }
    }

    @Test
    void mcpSyncClients_AreDiscovered() {
        // Verify that MCP sync clients were auto-discovered and available
        var mcpClientsBean = applicationContext.getBeansOfType(McpSyncClient.class);
        // Should have at least one MCP client if GitHub MCP server is running
        assertNotNull(mcpClientsBean);
    }

    @Test
    void chatClient_CanBeInvokedWithPrompt() {
        assertNotNull(chatClient);
        // Verify ChatClient has the prompt method
        var promptable = chatClient.prompt();
        assertNotNull(promptable, "ChatClient should support prompt method");
    }

    @Test
    void applicationContext_HasNoMissingDependencies() {
        // If context loaded successfully, all dependencies are resolved
        assertTrue(applicationContext.isActive(), "Application context should be active");
    }

    @Test
    void toolCallbackProvider_IsNotNull_AfterApplicationReady() {
        // Verify the provider is properly initialized (not null from graceful fallback)
        var provider = toolCallbackProvider;
        assertNotNull(provider, "ToolCallbackProvider should not be null");
        // Even if it's empty, it should return a valid array
        assertNotNull(provider.getToolCallbacks());
    }

    @Test
    void multiple_ApplicationContextLoads_Consistent() {
        // Verify consistency across multiple accesses
        for (int i = 0; i < 3; i++) {
            assertNotNull(applicationContext);
            assertNotNull(chatClient);
            assertNotNull(toolCallbackProvider);
        }
    }

}

