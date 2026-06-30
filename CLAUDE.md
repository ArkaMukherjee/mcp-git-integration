# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Spring AI MCP Client** is a Spring Boot application that integrates Claude (via Spring AI with Anthropic) with GitHub's Model Context Protocol (MCP) server. The MCP client enables the AI to access GitHub tools (repository search, issue listing, etc.) through a unified interface.

## Prerequisites

- **Java 21** — Required for this project (configured in `pom.xml`)
- **Maven 3.6+** — For building and running the application
- **Node.js 16+** — Required to run the GitHub MCP server (`@modelcontextprotocol/server-github`)

## Build and Development Commands

### Maven Commands

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Run only unit tests (Surefire)
mvn test

# Run only integration tests (Failsafe)
mvn integration-test

# Run all tests (unit + integration, with verification)
mvn verify

# Run a specific unit test class
mvn test -Dtest=ChatControllerTests

# Run a specific integration test class
mvn verify -Dit.test=McpClientApplicationIntegrationTests

# Run tests and generate code coverage report (requires 30% minimum coverage)
mvn verify jacoco:report

# View dependency tree
mvn dependency:tree
```

**Test Separation:**
- **Unit Tests** (`mvn test`): Runs `*Tests.java` files (e.g., `ChatControllerTests`)
  - Uses Surefire plugin
  - Excludes `*IntegrationTests.java` and `*ComprehensiveTests.java`
  
- **Integration Tests** (`mvn integration-test` or `mvn verify`): Runs `*IntegrationTests.java` and `*ComprehensiveTests.java` files
  - Uses Failsafe plugin
  - Full Spring Boot context loaded

**Code Coverage**: This project enforces a minimum of 30% line coverage via JaCoCo. Coverage reports are generated at `target/site/jacoco/index.html`.

### Quick Development Setup

1. **Environment Setup**: Set required environment variables before running:
   ```bash
   # Windows PowerShell
   $env:GITHUB_PERSONAL_ACCESS_TOKEN="your-github-token"
   $env:ANTHROPIC_API_KEY="your-claude-api-key"
   
   # Or Linux/Mac
   export GITHUB_PERSONAL_ACCESS_TOKEN="your-github-token"
   export ANTHROPIC_API_KEY="your-claude-api-key"
   ```

2. **Install GitHub MCP Server**: The application expects `@github/mcp-server` npm package to be installed globally or locally:
   ```bash
   npm install -g @modelcontextprotocol/server-github
   # OR in project directory
   npm install -g @modelcontextprotocol/server-github
   ```

3. **Start Application**: 
   ```bash
   mvn spring-boot:run
   ```
   Application will start on `http://localhost:8083`

## Architecture

### High-Level Design

The application follows a layered architecture:

```
ChatController (REST API)
    ↓
ChatClient (Spring AI - abstracts LLM interaction)
    ↓
SyncMcpToolCallbackProvider (Bridges Spring AI with MCP tools)
    ↓
McpSyncClient (MCP protocol client, auto-discovered)
    ↓
GitHub MCP Server (via STDIO protocol)
```

### Package Structure

```
src/main/java/com/github/mcp/client/
├── McpClientApplication.java       — Application entry point
├── config/
│   └── McpConfiguration.java       — Spring configuration (MCP + ChatClient setup)
└── controller/
    └── ChatController.java         — REST API endpoints
```

### Key Components

**McpConfiguration** (`src/main/java/com/github/mcp/client/config/McpConfiguration.java`)
- Initializes the `ChatClient` bean with MCP tools via `defaultTools()`
- Uses `SyncMcpToolCallbackProvider` to wrap `McpSyncClient` and expose MCP tools as Spring AI `ToolCallback` objects
- Gracefully handles missing MCP servers by returning empty tool providers (allows startup to succeed)
- Logs all discovered tools at startup (triggered by `ApplicationReadyEvent`) for debugging
- **Note**: Graceful degradation means the app will start even if GitHub MCP server fails to initialize

**ChatController** (`src/main/java/com/github/mcp/client/controller/ChatController.java`)
- REST API endpoints:
  - `GET /api/mcp/ask?prompt=<query>` — Send a prompt to Claude; tools are invoked automatically if needed
  - `GET /api/mcp/health` — Health check endpoint
- Exception handling for invalid prompts and server errors
- Logs all incoming prompts and responses at INFO level

### MCP Integration

- **MCP Client Type**: Synchronous (`McpSyncClient`)
- **Transport Protocol**: STDIO (standard input/output)
- **MCP Server**: GitHub MCP Server (`@github/mcp-server`)
- **Tool Discovery**: Automatic at startup; tools are logged to console

The `SyncMcpToolCallbackProvider` automatically registers all available MCP tools with Spring AI, so they're accessible to the Claude model when processing prompts.

## Configuration

### Application Configuration

**File**: `src/main/resources/application.properties`

Key properties:
- `server.port=8083` — Application runs on port 8083
- `spring.ai.mcp.client.stdio.connections.github-server.*` — MCP client connection to GitHub server via STDIO
  - Uses `npm.cmd` (Windows) or `npm` (Linux/Mac) to launch `@github/mcp-server`
  - Requires `GITHUB_PERSONAL_ACCESS_TOKEN` environment variable
- `spring.ai.anthropic.api-key` — Claude API key (from `ANTHROPIC_API_KEY` env var)
- Logging is configured at DEBUG level for MCP and Spring AI components

### Environment Variables Required

- `GITHUB_PERSONAL_ACCESS_TOKEN` — GitHub API token for accessing repositories and issues
- `ANTHROPIC_API_KEY` — Anthropic API key for Claude model

## Dependencies

### Core Framework
- **Spring Boot 4.1.0** — Web framework, embedded server, auto-configuration
- **Spring Boot Actuator** — Health checks and monitoring endpoints (e.g., `/actuator/health`)
- **Spring AI 2.0.0** — LLM integration framework with tool callback support and MCP integration

### AI & MCP Integration
- **spring-ai-starter-mcp-client** — MCP client with STDIO protocol support for connecting to MCP servers
- **spring-ai-starter-model-anthropic** — Claude (Anthropic) model integration

### Build & Code Quality
- **JaCoCo 0.8.10** — Code coverage reporting (enforces minimum 30% line coverage)
- **Lombok 1.18.36** — Annotation processor for reducing boilerplate code

### Testing
- **spring-boot-starter-test** — JUnit 5, Mockito, AssertJ, MockMvc
- **Mockito** — Mocking framework for unit tests

## Testing

Tests are located in `src/test/java/com/github/mcp/client/`:

### Unit Tests (Surefire)
Executed with `mvn test`. Excluded from default build to keep test runs fast.

- **ChatControllerTests** — Unit tests for the REST API endpoint using mocked ChatClient
  - Tests `/api/mcp/ask` endpoint with various prompts and error scenarios
  - Tests `/api/mcp/health` endpoint
  - Uses `@Mock` and `@Nested` classes for organization
  - Run: `mvn test` or `mvn test -Dtest=ChatControllerTests`

### Integration Tests (Failsafe)
Executed with `mvn verify` or `mvn integration-test`. Includes full Spring Boot context.

- **McpClientApplicationIntegrationTests** — Integration tests with Spring Boot context
  - Tests full application startup and bean initialization
  - Uses `@SpringBootTest` annotation
  - Run: `mvn verify` or `mvn verify -Dit.test=McpClientApplicationIntegrationTests`
  
- **McpClientApplicationComprehensiveTests** — Comprehensive tests for MCP configuration and tool discovery
  - Tests ToolCallbackProvider initialization and fallback behavior
  - Tests MCP tool logging on ApplicationReadyEvent
  - Tests all Spring beans are properly wired
  - Uses `@SpringBootTest` and `@Autowired` annotations
  - Run: `mvn verify` or `mvn verify -Dit.test=McpClientApplicationComprehensiveTests`

**Build Lifecycle:**
- `mvn test` — Unit tests only (fast)
- `mvn integration-test` — Integration tests only
- `mvn verify` — Unit tests + integration tests + verification
- `mvn clean install` — Full build with all tests

**Code Coverage Requirement**: Minimum 30% line coverage is enforced by JaCoCo during test execution.

## Common Development Tasks

### Testing the API

```bash
# Start the application
mvn spring-boot:run

# In another terminal, test the health endpoint
curl "http://localhost:8083/api/mcp/health"

# Test a question (URL-encoded prompt)
curl "http://localhost:8083/api/mcp/ask?prompt=List%20recent%20issues%20in%20spring-projects/spring-ai"

# View Spring Boot Actuator health details
curl "http://localhost:8083/actuator/health"
```

### Debugging MCP Tool Discovery

If the application starts but MCP tools are not available:

1. **Check logs**: Application logs discovered tools at startup with prefix `=== MCP Tools discovered from GitHub server: ===`
2. **Verify GitHub MCP server is installed**:
   ```bash
   npm list -g @modelcontextprotocol/server-github
   # If not installed:
   npm install -g @modelcontextprotocol/server-github
   ```
3. **Verify environment variables**:
   ```bash
   # Windows PowerShell
   echo $env:GITHUB_PERSONAL_ACCESS_TOKEN
   echo $env:ANTHROPIC_API_KEY
   
   # Linux/Mac
   echo $GITHUB_PERSONAL_ACCESS_TOKEN
   echo $ANTHROPIC_API_KEY
   ```
4. **Test MCP server directly**:
   ```bash
   npm exec -- @modelcontextprotocol/server-github
   # If it starts, the MCP server is properly configured
   ```

### Adding a New REST Endpoint

1. Add a method to `ChatController` with `@GetMapping` or `@PostMapping`
2. Inject and use `ChatClient` to process user requests
3. MCP tools are automatically available through `ChatClient` (no manual registration needed)
4. Add corresponding tests in `ChatControllerTests`

## Notes for Future Development

### Current Design
- **Auto-discovery**: MCP client connection is auto-discovered via Spring AI's `McpSyncClient` bean (no manual initialization required)
- **Automatic tool registration**: All MCP tools are automatically registered with ChatClient at startup via `SyncMcpToolCallbackProvider`
- **Model autonomy**: Claude automatically decides when to use MCP tools based on the prompt—no explicit tool invocation logic is needed
- **Graceful degradation**: If the GitHub MCP server fails to start, the application continues running without MCP tools (returns empty tool callbacks)

### Potential Enhancements
- **Async execution**: Current implementation is synchronous (`McpSyncClient`). Consider `McpAsyncClient` for better concurrency
- **WebFlux**: Consider migrating from Spring WebMvc to Spring WebFlux (reactive) for handling concurrent requests more efficiently
- **Error recovery**: Add automatic reconnection logic if MCP server becomes unavailable
- **Tool result streaming**: Current implementation waits for full tool results. Consider streaming long-running tool responses
- **Multiple MCP servers**: Extend architecture to support multiple MCP servers (e.g., GitHub + custom servers)
- **Request/response logging**: Add structured logging for audit trails and debugging