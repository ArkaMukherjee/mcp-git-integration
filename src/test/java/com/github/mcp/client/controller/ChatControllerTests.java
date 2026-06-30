package com.github.mcp.client.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ChatControllerTests {

    private ChatController chatController;

    @Mock
    private ChatClient mockChatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec mockRequestSpec;

    @Mock
    private ChatClient.CallResponseSpec mockCallResponseSpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatController = new ChatController(mockChatClient);
    }

    @Nested
    @DisplayName("Health Endpoint Tests")
    class HealthEndpointTests {
        @Test
        @DisplayName("Should return 200 OK when health check is called")
        void healthEndpoint_ReturnsOk() {
            ResponseEntity<String> response = chatController.health();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("running on port 8083"));
        }

        @Test
        @DisplayName("Health endpoint should always return exact message")
        void healthEndpoint_ReturnsExactMessage() {
            ResponseEntity<String> response = chatController.health();
            assertEquals("MCP Client is running on port 8083", response.getBody());
        }

        @Test
        @DisplayName("Multiple health checks should return consistent results")
        void healthEndpoint_MultipleCallsConsistent() {
            ResponseEntity<String> response1 = chatController.health();
            ResponseEntity<String> response2 = chatController.health();
            ResponseEntity<String> response3 = chatController.health();

            assertEquals(response1.getStatusCode(), response2.getStatusCode());
            assertEquals(response2.getStatusCode(), response3.getStatusCode());
            assertEquals(response1.getBody(), response2.getBody());
            assertEquals(response2.getBody(), response3.getBody());
        }
    }

    @Nested
    @DisplayName("Ask AI Endpoint - Valid Input Tests")
    class AskAiValidInputTests {
        @Test
        @DisplayName("Should handle valid prompt and return response")
        void askEndpoint_WithValidPrompt_ReturnsResponse() {
            String testPrompt = "Test prompt";
            String expectedResponse = "Test response from Claude";

            when(mockChatClient.prompt(testPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(expectedResponse);

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
            verify(mockChatClient, times(1)).prompt(testPrompt);
        }

        @Test
        @DisplayName("Should handle empty prompt")
        void askEndpoint_WithEmptyPrompt_ReturnsResponse() {
            String testPrompt = "";
            String expectedResponse = "Response to empty prompt";

            when(mockChatClient.prompt(testPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(expectedResponse);

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }

        @Test
        @DisplayName("Should handle long prompt")
        void askEndpoint_WithLongPrompt_ReturnsResponse() {
            String longPrompt = "a".repeat(1000);
            String expectedResponse = "Response to long prompt";

            when(mockChatClient.prompt(longPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(expectedResponse);

            ResponseEntity<String> response = chatController.askAi(longPrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }

        @Test
        @DisplayName("Should handle prompt with special characters")
        void askEndpoint_WithSpecialCharacters_ReturnsResponse() {
            String specialPrompt = "What is 2+2? !@#$%^&*()_+-=[]{}|;:',.<>?/";
            String expectedResponse = "The answer is 4";

            when(mockChatClient.prompt(specialPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(expectedResponse);

            ResponseEntity<String> response = chatController.askAi(specialPrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }

        @Test
        @DisplayName("Should handle prompt with multiline text")
        void askEndpoint_WithMultilinePrompt_ReturnsResponse() {
            String multilinePrompt = "Line 1\nLine 2\nLine 3";
            String expectedResponse = "Response to multiline";

            when(mockChatClient.prompt(multilinePrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(expectedResponse);

            ResponseEntity<String> response = chatController.askAi(multilinePrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }

        @Test
        @DisplayName("Should handle prompt with unicode characters")
        void askEndpoint_WithUnicodePrompt_ReturnsResponse() {
            String unicodePrompt = "Hello 世界 🌍 مرحبا";
            String expectedResponse = "Response with unicode";

            when(mockChatClient.prompt(unicodePrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(expectedResponse);

            ResponseEntity<String> response = chatController.askAi(unicodePrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
        }

        @ParameterizedTest
        @DisplayName("Should handle various valid prompts")
        @ValueSource(strings = {"Hello", "What is AI?", "123", "test@email.com", "http://example.com"})
        void askEndpoint_WithVariousValidPrompts(String prompt) {
            String expectedResponse = "Generic response";

            when(mockChatClient.prompt(prompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(expectedResponse);

            ResponseEntity<String> response = chatController.askAi(prompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(expectedResponse, response.getBody());
            verify(mockChatClient).prompt(prompt);
        }
    }

    @Nested
    @DisplayName("Ask AI Endpoint - Exception Handling Tests")
    class AskAiExceptionHandlingTests {
        @Test
        @DisplayName("Should handle RuntimeException from ChatClient")
        void askEndpoint_WhenChatClientThrowsRuntimeException_ReturnsInternalServerError() {
            String testPrompt = "Test prompt";
            String errorMessage = "Connection timeout";

            when(mockChatClient.prompt(testPrompt))
                    .thenThrow(new RuntimeException(errorMessage));

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Error:"));
            assertTrue(response.getBody().contains(errorMessage));
        }

        @Test
        @DisplayName("Should handle NullPointerException")
        void askEndpoint_WhenChatClientThrowsNullPointerException_ReturnsInternalServerError() {
            String testPrompt = "Test prompt";

            when(mockChatClient.prompt(testPrompt))
                    .thenThrow(new NullPointerException("Null reference"));

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains("Null reference"));
        }

        @Test
        @DisplayName("Should handle IllegalStateException")
        void askEndpoint_WhenChatClientThrowsIllegalStateException_ReturnsInternalServerError() {
            String testPrompt = "Test prompt";
            String errorMessage = "Invalid state";

            when(mockChatClient.prompt(testPrompt))
                    .thenThrow(new IllegalStateException(errorMessage));

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains(errorMessage));
        }

        @Test
        @DisplayName("Should handle exception from call() method")
        void askEndpoint_WhenCallThrowsException_ReturnsInternalServerError() {
            String testPrompt = "Test prompt";
            String errorMessage = "Call failed";

            when(mockChatClient.prompt(testPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenThrow(new RuntimeException(errorMessage));

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains(errorMessage));
        }

        @Test
        @DisplayName("Should handle exception from content() method")
        void askEndpoint_WhenContentThrowsException_ReturnsInternalServerError() {
            String testPrompt = "Test prompt";
            String errorMessage = "Content extraction failed";

            when(mockChatClient.prompt(testPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenThrow(new RuntimeException(errorMessage));

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains(errorMessage));
        }

        @Test
        @DisplayName("Should handle generic Exception")
        void askEndpoint_WhenGenericExceptionThrown_ReturnsInternalServerError() {
            String testPrompt = "Test prompt";
            String errorMessage = "Generic error occurred";

            when(mockChatClient.prompt(testPrompt))
                    .thenThrow(new RuntimeException(errorMessage));

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.getBody().contains(errorMessage));
        }

        @Test
        @DisplayName("Should handle exception with null message")
        void askEndpoint_WhenExceptionHasNullMessage_ReturnsInternalServerError() {
            String testPrompt = "Test prompt";

            when(mockChatClient.prompt(testPrompt))
                    .thenThrow(new RuntimeException((String) null));

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("Ask AI Endpoint - Null Response Tests")
    class AskAiNullResponseTests {
        @Test
        @DisplayName("Should handle null response content")
        void askEndpoint_WhenResponseIsNull_ReturnsOkWithNull() {
            String testPrompt = "Test prompt";

            when(mockChatClient.prompt(testPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(null);

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNull(response.getBody());
        }

        @Test
        @DisplayName("Should handle empty string response")
        void askEndpoint_WhenResponseIsEmptyString_ReturnsOkWithEmpty() {
            String testPrompt = "Test prompt";

            when(mockChatClient.prompt(testPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn("");

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("", response.getBody());
        }

        @Test
        @DisplayName("Should handle whitespace-only response")
        void askEndpoint_WhenResponseIsWhitespace_ReturnsOkWithWhitespace() {
            String testPrompt = "Test prompt";
            String whitespaceResponse = "   \n\t  ";

            when(mockChatClient.prompt(testPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(whitespaceResponse);

            ResponseEntity<String> response = chatController.askAi(testPrompt);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(whitespaceResponse, response.getBody());
        }
    }

    @Nested
    @DisplayName("Exception Handler Tests")
    class ExceptionHandlerTests {
        @Test
        @DisplayName("Should handle IllegalArgumentException with specific message")
        void handleInvalidPrompt_WithIllegalArgumentException_ReturnsBadRequest() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid input");

            ResponseEntity<String> response = chatController.handleInvalidPrompt(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Invalid prompt"));
            assertTrue(response.getBody().contains("Invalid input"));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with null message")
        void handleInvalidPrompt_WithNullMessage_ReturnsBadRequest() {
            IllegalArgumentException exception = new IllegalArgumentException((String) null);

            ResponseEntity<String> response = chatController.handleInvalidPrompt(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains("Invalid prompt"));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with empty message")
        void handleInvalidPrompt_WithEmptyMessage_ReturnsBadRequest() {
            IllegalArgumentException exception = new IllegalArgumentException("");

            ResponseEntity<String> response = chatController.handleInvalidPrompt(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains("Invalid prompt"));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException with special characters in message")
        void handleInvalidPrompt_WithSpecialCharactersInMessage_ReturnsBadRequest() {
            String messageWithSpecialChars = "Invalid: !@#$%^&*()";
            IllegalArgumentException exception = new IllegalArgumentException(messageWithSpecialChars);

            ResponseEntity<String> response = chatController.handleInvalidPrompt(exception);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(response.getBody().contains(messageWithSpecialChars));
        }
    }

    @Nested
    @DisplayName("Constructor and Initialization Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Should initialize ChatController with ChatClient")
        void chatControllerConstructor_InitializesWithChatClient() {
            assertNotNull(chatController);
            Object injectedClient = ReflectionTestUtils.getField(chatController, "chatClient");
            assertNotNull(injectedClient);
            assertSame(mockChatClient, injectedClient);
        }

        @Test
        @DisplayName("Should not allow null ChatClient")
        void chatControllerConstructor_WithNullChatClient_StoresNull() {
            ChatController controllerWithNull = new ChatController(null);
            assertNotNull(controllerWithNull);
        }
    }

    @Nested
    @DisplayName("Multiple Calls and State Tests")
    class MultipleCallsTests {
        @Test
        @DisplayName("Should handle multiple successive successful calls")
        void askEndpoint_MultipleSuccessiveCalls_AllSucceed() {
            String prompt1 = "First prompt";
            String response1 = "First response";
            String prompt2 = "Second prompt";
            String response2 = "Second response";

            when(mockChatClient.prompt(prompt1))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(response1);

            ResponseEntity<String> result1 = chatController.askAi(prompt1);
            assertEquals(HttpStatus.OK, result1.getStatusCode());
            assertEquals(response1, result1.getBody());

            when(mockChatClient.prompt(prompt2))
                    .thenReturn(mockRequestSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(response2);

            ResponseEntity<String> result2 = chatController.askAi(prompt2);
            assertEquals(HttpStatus.OK, result2.getStatusCode());
            assertEquals(response2, result2.getBody());

            verify(mockChatClient, times(2)).prompt(anyString());
        }

        @Test
        @DisplayName("Should handle mixed success and failure calls")
        void askEndpoint_MixedSuccessAndFailureCalls() {
            String successPrompt = "Success";
            String failurePrompt = "Failure";

            when(mockChatClient.prompt(successPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn("Success response");

            ResponseEntity<String> successResult = chatController.askAi(successPrompt);
            assertEquals(HttpStatus.OK, successResult.getStatusCode());

            when(mockChatClient.prompt(failurePrompt))
                    .thenThrow(new RuntimeException("Failed"));

            ResponseEntity<String> failureResult = chatController.askAi(failurePrompt);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failureResult.getStatusCode());
        }

        @Test
        @DisplayName("Should maintain state consistency across calls")
        void askEndpoint_StateConsistencyAcrossCalls() {
            String prompt = "Test";
            String response = "Response";

            when(mockChatClient.prompt(prompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(response);

            ResponseEntity<String> result1 = chatController.askAi(prompt);
            ResponseEntity<String> result2 = chatController.askAi(prompt);
            ResponseEntity<String> result3 = chatController.askAi(prompt);

            assertEquals(result1.getStatusCode(), result2.getStatusCode());
            assertEquals(result2.getStatusCode(), result3.getStatusCode());
            assertEquals(result1.getBody(), result2.getBody());
            assertEquals(result2.getBody(), result3.getBody());
        }
    }

    @Nested
    @DisplayName("Response Body Tests")
    class ResponseBodyTests {
        @Test
        @DisplayName("Should preserve response content exactly")
        void askEndpoint_PreservesResponseContentExactly() {
            String prompt = "Test";
            String responseContent = "Exact response with newlines\n\nand special chars !@#$";

            when(mockChatClient.prompt(prompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(responseContent);

            ResponseEntity<String> response = chatController.askAi(prompt);

            assertEquals(responseContent, response.getBody());
            assertTrue(response.getBody().contains("\n\n"));
        }

        @Test
        @DisplayName("Should preserve error message exactly")
        void askEndpoint_PreservesErrorMessageExactly() {
            String prompt = "Test";
            String errorMsg = "Specific error: Invalid API key";

            when(mockChatClient.prompt(prompt))
                    .thenThrow(new RuntimeException(errorMsg));

            ResponseEntity<String> response = chatController.askAi(prompt);

            assertTrue(response.getBody().contains(errorMsg));
        }

        @Test
        @DisplayName("Should format error response correctly")
        void askEndpoint_FormatsErrorResponseCorrectly() {
            String prompt = "Test";
            String errorMsg = "Connection failed";

            when(mockChatClient.prompt(prompt))
                    .thenThrow(new RuntimeException(errorMsg));

            ResponseEntity<String> response = chatController.askAi(prompt);

            assertTrue(response.getBody().startsWith("Error:"));
            assertTrue(response.getBody().contains(errorMsg));
        }
    }

    @Nested
    @DisplayName("Prompt Processing Tests")
    class PromptProcessingTests {
        @Test
        @DisplayName("Should pass prompt to ChatClient without modification")
        void askEndpoint_PassesPromptUnmodified() {
            String prompt = "Test prompt with spaces   and\ttabs";
            String response = "Response";

            when(mockChatClient.prompt(prompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(response);

            chatController.askAi(prompt);

            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            verify(mockChatClient).prompt(captor.capture());
            assertEquals(prompt, captor.getValue());
        }

        @Test
        @DisplayName("Should handle very long prompts")
        void askEndpoint_HandlesVeryLongPrompts() {
            String longPrompt = "a".repeat(10000);
            String response = "Response";

            when(mockChatClient.prompt(longPrompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn(response);

            ResponseEntity<String> result = chatController.askAi(longPrompt);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(response, result.getBody());
        }

        @Test
        @DisplayName("Should handle prompts with newlines at boundaries")
        void askEndpoint_HandlesNewlinesAtBoundaries() {
            String[] prompts = {"\nPrompt", "Prompt\n", "\nPrompt\n", "Prompt\n\n\nEnd"};

            for (String prompt : prompts) {
                when(mockChatClient.prompt(prompt))
                        .thenReturn(mockRequestSpec);
                when(mockRequestSpec.call())
                        .thenReturn(mockCallResponseSpec);
                when(mockCallResponseSpec.content())
                        .thenReturn("Response");

                ResponseEntity<String> response = chatController.askAi(prompt);
                assertEquals(HttpStatus.OK, response.getStatusCode());
            }
        }
    }

    @Nested
    @DisplayName("Response Entity Tests")
    class ResponseEntityTests {
        @Test
        @DisplayName("Should return ResponseEntity with OK status for success")
        void askEndpoint_ReturnsResponseEntityWithOkStatus() {
            String prompt = "Test";
            when(mockChatClient.prompt(prompt))
                    .thenReturn(mockRequestSpec);
            when(mockRequestSpec.call())
                    .thenReturn(mockCallResponseSpec);
            when(mockCallResponseSpec.content())
                    .thenReturn("Response");

            ResponseEntity<String> response = chatController.askAi(prompt);

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.hasBody());
        }

        @Test
        @DisplayName("Should return ResponseEntity with INTERNAL_SERVER_ERROR for exception")
        void askEndpoint_ReturnsResponseEntityWithErrorStatus() {
            String prompt = "Test";
            when(mockChatClient.prompt(prompt))
                    .thenThrow(new RuntimeException("Error"));

            ResponseEntity<String> response = chatController.askAi(prompt);

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(response.hasBody());
        }

        @Test
        @DisplayName("Health endpoint returns ResponseEntity with OK status")
        void healthEndpoint_ReturnsResponseEntityWithOkStatus() {
            ResponseEntity<String> response = chatController.health();

            assertNotNull(response);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.hasBody());
        }
    }
}

