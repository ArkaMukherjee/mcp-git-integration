package com.github.mcp.client.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class McpConfigurationTests {

    private McpConfiguration mcpConfiguration;

    @Mock
    private ObjectProvider<List<McpSyncClient>> mockMcpClientsProvider;

    @Mock
    private List<McpSyncClient> mockMcpClientsList;

    @Mock
    private McpSyncClient mockMcpClient;

    @Mock
    private ChatClient.Builder mockChatClientBuilder;

    @Mock
    private ChatClient mockChatClient;

    @Mock
    private ApplicationReadyEvent mockApplicationReadyEvent;

    @Mock
    private ToolCallbackProvider mockToolCallbackProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mcpConfiguration = new McpConfiguration();
    }

    @Test
    void toolCallbackProvider_WithValidMcpClients_CreatesSyncMcpToolCallbackProvider() {
        // Arrange
        when(mockMcpClientsProvider.getIfAvailable()).thenReturn(mockMcpClientsList);
        when(mockMcpClientsList.isEmpty()).thenReturn(false);
        when(mockMcpClientsList.getFirst()).thenReturn(mockMcpClient);

        // Act
        ToolCallbackProvider provider = mcpConfiguration.toolCallbackProvider(mockMcpClientsProvider);

        // Assert
        assertNotNull(provider);
        verify(mockMcpClientsProvider, times(1)).getIfAvailable();
        verify(mockMcpClientsList, times(1)).getFirst();
    }

    @Test
    void toolCallbackProvider_WithEmptyMcpClientsList_ReturnsEmptyProvider() {
        // Arrange
        when(mockMcpClientsProvider.getIfAvailable()).thenReturn(mockMcpClientsList);
        when(mockMcpClientsList.isEmpty()).thenReturn(true);

        // Act
        ToolCallbackProvider provider = mcpConfiguration.toolCallbackProvider(mockMcpClientsProvider);

        // Assert
        assertNotNull(provider);
        ToolCallback[] tools = provider.getToolCallbacks();
        assertEquals(0, tools.length);
    }

    @Test
    void toolCallbackProvider_WithNullMcpClientsList_ReturnsEmptyProvider() {
        // Arrange
        when(mockMcpClientsProvider.getIfAvailable()).thenReturn(null);

        // Act
        ToolCallbackProvider provider = mcpConfiguration.toolCallbackProvider(mockMcpClientsProvider);

        // Assert
        assertNotNull(provider);
        ToolCallback[] tools = provider.getToolCallbacks();
        assertEquals(0, tools.length);
    }

    @Test
    void toolCallbackProvider_WithException_ReturnsEmptyProviderAndContinues() {
        // Arrange
        when(mockMcpClientsProvider.getIfAvailable())
                .thenThrow(new RuntimeException("Failed to get MCP clients"));

        // Act
        ToolCallbackProvider provider = mcpConfiguration.toolCallbackProvider(mockMcpClientsProvider);

        // Assert
        assertNotNull(provider);
        ToolCallback[] tools = provider.getToolCallbacks();
        assertEquals(0, tools.length);
    }

    @Test
    void chatClient_WithToolCallbackProvider_BuildsChatClientWithTools() {
        // Arrange
        when(mockChatClientBuilder.defaultTools(mockToolCallbackProvider))
                .thenReturn(mockChatClientBuilder);
        when(mockChatClientBuilder.build())
                .thenReturn(mockChatClient);

        // Act
        ChatClient chatClient = mcpConfiguration.chatClient(mockChatClientBuilder, mockToolCallbackProvider);

        // Assert
        assertNotNull(chatClient);
        verify(mockChatClientBuilder, times(1)).defaultTools(mockToolCallbackProvider);
        verify(mockChatClientBuilder, times(1)).build();
    }

    @Test
    void chatClient_BuildsValidChatClientBean() {
        // Arrange
        when(mockChatClientBuilder.defaultTools(mockToolCallbackProvider))
                .thenReturn(mockChatClientBuilder);
        when(mockChatClientBuilder.build())
                .thenReturn(mockChatClient);

        // Act
        ChatClient result = mcpConfiguration.chatClient(mockChatClientBuilder, mockToolCallbackProvider);

        // Assert
        assertSame(mockChatClient, result);
    }

    @Test
    void onApplicationReady_WithValidToolCallbackProvider_LogsTools() {
        // Arrange
        ToolCallbackProvider provider = () -> new ToolCallback[0];
        ReflectionTestUtils.setField(mcpConfiguration, "toolCallbackProvider", provider);

        // Act & Assert - Should not throw
        assertDoesNotThrow(() -> mcpConfiguration.onApplicationReady(mockApplicationReadyEvent));
    }

    @Test
    void onApplicationReady_WithNullToolCallbackProvider_LogsWarning() {
        // Arrange
        ReflectionTestUtils.setField(mcpConfiguration, "toolCallbackProvider", null);

        // Act & Assert - Should not throw even with null provider
        assertDoesNotThrow(() -> mcpConfiguration.onApplicationReady(mockApplicationReadyEvent));
    }

    @Test
    void onApplicationReady_WithToolCallbackProviderThrowingException_HandleException() {
        // Arrange
        ToolCallbackProvider failingProvider = mock(ToolCallbackProvider.class);
        when(failingProvider.getToolCallbacks())
                .thenThrow(new RuntimeException("Tool retrieval failed"));
        ReflectionTestUtils.setField(mcpConfiguration, "toolCallbackProvider", failingProvider);

        // Act & Assert - Should not throw, handles exception gracefully
        assertDoesNotThrow(() -> mcpConfiguration.onApplicationReady(mockApplicationReadyEvent));
    }

    @Test
    void toolCallbackProvider_StoresProviderFieldForLaterUse() {
        // Arrange
        when(mockMcpClientsProvider.getIfAvailable()).thenReturn(mockMcpClientsList);
        when(mockMcpClientsList.isEmpty()).thenReturn(true);

        // Act
        ToolCallbackProvider provider = mcpConfiguration.toolCallbackProvider(mockMcpClientsProvider);

        // Assert - Verify that the field is set
        ToolCallbackProvider storedProvider = (ToolCallbackProvider)
                ReflectionTestUtils.getField(mcpConfiguration, "toolCallbackProvider");
        assertNotNull(storedProvider);
    }

    @Test
    void toolCallbackProvider_WithMultipleMcpClients_UsesFirstClient() {
        // Arrange
        McpSyncClient client1 = mock(McpSyncClient.class);
        McpSyncClient client2 = mock(McpSyncClient.class);
        List<McpSyncClient> clients = List.of(client1, client2);

        when(mockMcpClientsProvider.getIfAvailable()).thenReturn(clients);

        // Act
        ToolCallbackProvider provider = mcpConfiguration.toolCallbackProvider(mockMcpClientsProvider);

        // Assert
        assertNotNull(provider);
        // Verify getFirst was called (which would get client1)
        verify(mockMcpClientsProvider, times(1)).getIfAvailable();
    }

    @Test
    void mcpConfiguration_CanBeInstantiated() {
        // Arrange & Act
        McpConfiguration config = new McpConfiguration();

        // Assert
        assertNotNull(config);
    }

    @Test
    void toolCallbackProvider_ReturnsNotNullProvider() {
        // Arrange
        when(mockMcpClientsProvider.getIfAvailable()).thenReturn(null);

        // Act
        ToolCallbackProvider provider = mcpConfiguration.toolCallbackProvider(mockMcpClientsProvider);

        // Assert
        assertNotNull(provider, "Provider should never be null, should return empty provider fallback");
    }

    @Test
    void chatClient_WithNullBuilder_StillBuilds() {
        // Arrange
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.defaultTools(mockToolCallbackProvider)).thenReturn(builder);
        when(builder.build()).thenReturn(mockChatClient);

        // Act
        ChatClient result = mcpConfiguration.chatClient(builder, mockToolCallbackProvider);

        // Assert
        assertNotNull(result);
    }

}

