# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Spring AI MCP Client** is a Spring Boot application that integrates Claude (via Spring AI with Anthropic) with GitHub's Model Context Protocol (MCP) server. The MCP client enables the AI to access GitHub tools (repository search, issue listing, etc.) through a unified interface.

## Build and Development Commands

### Maven Commands

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run

# Run tests
mvn test

# Run a specific test class
mvn test -Dtest=McpClientApplicationTests

# Run tests with coverage
mvn test jacoco:report

# Check for dependency vulnerabilities
mvn dependency-check:check

# View dependency tree
mvn dependency:tree
```

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
   npm install -g @github/mcp-server
   # OR in project directory
   npm install @github/mcp-server
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

### Key Components

**McpConfiguration** (`src/main/java/com/github/mcp/client/config/McpConfiguration.java`)
- Initializes the `ChatClient` bean with MCP tools
- Wraps `McpSyncClient` using `SyncMcpToolCallbackProvider` to expose MCP tools as Spring AI `ToolCallback` objects
- Logs all discovered tools at startup for debugging

**ChatController** (`src/main/java/com/github/mcp/client/controller/ChatController.java`)
- REST API endpoint: `GET /api/mcp/ask?prompt=<query>`
- Takes a user prompt, passes it to ChatClient
- ChatClient automatically selects and invokes GitHub MCP tools if needed
- Returns AI-generated response

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
- **Spring Boot 4.1.0** — Web framework and auto-configuration
- **Spring AI 2.0.0** — AI/LLM integration framework with tool callback support

### AI Models & MCP
- **spring-ai-starter-mcp-client** — MCP client implementation for Spring AI
- **spring-ai-starter-model-anthropic** — Claude (Anthropic) model integration

### Utilities
- **Lombok 1.18.36** — Annotation processor to reduce boilerplate (getters, setters, constructors)

### Testing
- **spring-boot-starter-webmvc-test** — Testing dependencies (JUnit, MockMvc, AssertJ)

## Testing

- Tests are located in `src/test/java/com/github/mcp/client/`
- Current test file: `McpClientApplicationTests.java` (basic Spring Boot context test)
- To expand: Add integration tests for ChatController and MCP tool invocation
- Run with: `mvn test`

## Common Development Tasks

### Testing the API

```bash
# After starting the application with mvn spring-boot:run
# In another terminal:

# Test asking a question
curl "http://localhost:8083/api/mcp/ask?prompt=List%20recent%20issues%20in%20spring-projects/spring-ai"
```

### Debugging MCP Tool Discovery

- Application logs all available tools at startup in DEBUG level
- Check console output or logs for `=== MCP Tools discovered from GitHub server: ===`
- If no tools are found, verify:
  - GitHub MCP server is installed (`npm install -g @github/mcp-server`)
  - `GITHUB_PERSONAL_ACCESS_TOKEN` is set
  - MCP server can be launched (test manually: `npm exec -- @github/mcp-server`)

### Adding a New REST Endpoint

1. Add method to `ChatController`
2. Use injected `ChatClient` to make requests
3. ChatClient automatically has access to all MCP tools via `SyncMcpToolCallbackProvider`

## Notes for Future Development

- MCP client connection is auto-discovered via Spring AI's `McpSyncClient` bean (no manual initialization needed)
- All MCP tools are automatically registered with ChatClient at startup
- No explicit tool invocation logic is needed—Claude decides when to use tools based on the prompt
- For production: Consider adding connection pooling, error recovery, and async tool execution
- Current implementation is synchronous; consider WebFlux/Reactive approach for better concurrency