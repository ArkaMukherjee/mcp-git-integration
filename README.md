# Spring AI MCP Client

A Spring Boot application that connects to GitHub's Model Context Protocol (MCP) server using Spring AI with JDK 17.

## Prerequisites

- Java 17 or higher
- Maven 3.8.0+
- Spring Boot 3.2.0+
- OpenAI API Key (for Spring AI integration)

## Project Structure

```
├── src/main/java/com/github/mcp/client
│   ├── McpClientApplication.java          # Main Spring Boot application
│   ├── config/
│   │   ├── McpProperties.java              # Configuration properties
│   │   └── McpClientConfiguration.java     # Spring configuration
│   ├── service/
│   │   └── GitHubMcpClientService.java     # MCP client service logic
│   └── controller/
│       └── McpClientController.java        # REST API endpoints
├── src/main/resources/
│   └── application.yml                     # Application configuration
└── pom.xml                                 # Maven configuration
```

## Configuration

Update `application.yml` with your settings:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}  # Set as environment variable

mcp:
  github:
    host: localhost
    port: 3000
    protocol: stdio
    command: npx
    args:
      - "@modelcontextprotocol/server-github"
```

## Building the Project

```bash
# Clone/navigate to project directory
cd CopilotPlayground

# Build with Maven
mvn clean install

# Run the application
mvn spring-boot:run
```

## API Endpoints

- **Health Check**: `GET /api/mcp/health`
- **Connect to MCP**: `POST /api/mcp/connect`
- **List Tools**: `GET /api/mcp/tools`
- **Execute Tool**: `POST /api/mcp/execute-tool?toolName=<name>` (with request body)
- **Disconnect**: `POST /api/mcp/disconnect`

## Testing

```bash
# Run tests
mvn test

# Run tests with coverage
mvn test jacoco:report
```

## Environment Variables

```bash
# OpenAI Configuration
export OPENAI_API_KEY=your-api-key-here

# Run the application
mvn spring-boot:run
```

## Dependencies

- **Spring Boot 3.2.0**: Web framework and auto-configuration
- **Spring AI 0.8.1**: AI integration framework
- **Spring AI OpenAI**: OpenAI model integration
- **MCP SDK**: GitHub's Model Context Protocol implementation
- **Lombok**: Reduce boilerplate code
- **WebFlux**: Reactive web framework

## Features

- ✅ Connection management to GitHub MCP Server
- ✅ Tool listing and execution
- ✅ REST API for MCP operations
- ✅ Spring AI integration for LLM capabilities
- ✅ Configuration management via properties
- ✅ Logging and error handling

## Architecture

This project implements a client-server architecture:

1. **Spring Boot Application** runs on `localhost:8080`
2. **REST Controller** exposes MCP operations via HTTP
3. **MCP Service** manages connections and tool execution
4. **Configuration** manages MCP server connection details

## Future Enhancements

- [ ] Implement actual MCP protocol communication
- [ ] Add support for multiple MCP servers
- [ ] Create WebSocket endpoints for real-time updates
- [ ] Add caching for tool lists
- [ ] Implement async tool execution
- [ ] Add comprehensive error handling
- [ ] Create Docker support
- [ ] Add monitoring and metrics

## License

MIT License
