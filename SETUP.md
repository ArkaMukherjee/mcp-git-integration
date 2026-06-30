# Setup Guide: Spring AI MCP Client with GitHub Integration

This guide helps you set up and run the Spring AI MCP Client that integrates Claude with GitHub's Model Context Protocol (MCP) server.

## Prerequisites

- **Java 21+** (System has Java 26 ✓)
- **Maven 3.8.0+** (Required - needs installation)
- **Node.js 18+** (Required for GitHub MCP server)
- **npm** (Comes with Node.js)
- **GitHub Personal Access Token** (For GitHub API access)
- **Anthropic API Key** (For Claude integration)

---

## Step 1: Install Maven

Maven is required to build and run this project.

### Option A: Using Chocolatey (Windows)
```powershell
choco install maven
mvn --version  # Verify installation
```

### Option B: Manual Installation
1. Download from: https://maven.apache.org/download.cgi
2. Extract to a folder (e.g., `C:\Maven`)
3. Add `C:\Maven\apache-maven-X.X.X\bin` to system PATH
4. Verify: `mvn --version`

---

## Step 2: Install GitHub MCP Server

The application requires the GitHub MCP server to communicate with GitHub's API via MCP.

```powershell
npm install -g @github/mcp-server
```

**Verify installation:**
```powershell
npm list -g @github/mcp-server
```

Should output something like:
```
C:\Users\<username>\AppData\Roaming\npm
`-- @github/mcp-server@X.X.X
```

---

## Step 3: Set Environment Variables

The application requires two environment variables:

### GitHub Personal Access Token
1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click "Generate new token (classic)"
3. Give it a name, e.g., "MCP Integration"
4. Select scopes: `repo`, `read:user`, `read:org`
5. Click "Generate token" and copy it

### Anthropic API Key
1. Go to https://console.anthropic.com/
2. Click "API keys" in the left sidebar
3. Click "Create Key"
4. Copy the key

### Set Environment Variables (PowerShell)
```powershell
$env:GITHUB_PERSONAL_ACCESS_TOKEN = "ghp_xxxxxxxxxxxxxxxxxxxx"
$env:ANTHROPIC_API_KEY = "sk-ant-xxxxxxxxxxxxxxxx"
```

Or set them permanently in Windows:
1. Search for "Environment Variables" in Windows
2. Click "Edit the system environment variables"
3. Click "Environment Variables..."
4. Add two new user variables:
   - Name: `GITHUB_PERSONAL_ACCESS_TOKEN`, Value: `ghp_...`
   - Name: `ANTHROPIC_API_KEY`, Value: `sk-ant-...`
5. Click OK and restart your terminal

---

## Step 4: Build the Project

```bash
cd "D:\Workspace\Backend Workspace\mcp-git-integration"
mvn clean install
```

This will:
- Download all dependencies
- Compile the Java code
- Run tests
- Create a JAR file in `target/`

---

## Step 5: Run the Application

```bash
mvn spring-boot:run
```

Or run the compiled JAR:
```bash
java -jar target/spring-ai-mcp-client-1.0.0.jar
```

**Expected startup logs:**
```
=== MCP Tools discovered from GitHub server: X ===
  → [repo_search] : Search GitHub repositories
  → [list_issues] : List issues for a repository
  ...
MCP Client is running on port 8083
```

---

## Step 6: Test the API

### Health Check
```bash
curl http://localhost:8083/api/mcp/health
```

Expected response:
```
MCP Client is running on port 8083
```

### Ask Claude with GitHub Context
```bash
curl "http://localhost:8083/api/mcp/ask?prompt=Find%20recent%20issues%20in%20spring-projects/spring-ai%20related%20to%20AI%20integration"
```

Claude will:
1. Understand your request
2. Automatically invoke GitHub MCP tools if needed
3. Return a comprehensive answer using live GitHub data

---

## Troubleshooting

### Error: `Failed to instantiate [java.util.List]: Factory method 'mcpSyncClients' threw exception`

**Cause:** GitHub MCP server is not running or not installed

**Solution:**
```powershell
# 1. Check if GitHub MCP server is installed
npm list -g @github/mcp-server

# 2. If not installed:
npm install -g @github/mcp-server

# 3. Test the server directly
npm exec -- @github/mcp-server
```

### Error: `Client failed to initialize by explicit API call`

**Cause:** Environment variables not set

**Solution:**
```powershell
# Set environment variables in current session
$env:GITHUB_PERSONAL_ACCESS_TOKEN = "your-token"
$env:ANTHROPIC_API_KEY = "your-key"

# Restart the application
mvn spring-boot:run
```

### Error: `mvn: command not found`

**Cause:** Maven is not installed or not in PATH

**Solution:**
1. Install Maven using Chocolatey: `choco install maven`
2. Or download and install manually from https://maven.apache.org/

### Application starts but MCP tools not discovered

**Cause:** GitHub MCP server is not accessible

**Solution:**
1. Verify npx can run the GitHub server:
   ```powershell
   npx @github/mcp-server
   ```
2. Check that your GitHub token is valid and has repo scope
3. Check logs for detailed error messages

### Port 8083 already in use

**Solution:** Change the port in `application.properties`:
```properties
server.port=8084  # or any available port
```

---

## Architecture

```
┌─────────────────────────────┐
│   ChatController (REST)     │  ← POST /api/mcp/ask?prompt=...
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────┐
│   ChatClient (Spring AI)    │  ← Automatically uses MCP tools
└──────────────┬──────────────┘
               │
┌──────────────▼──────────────────────────┐
│ SyncMcpToolCallbackProvider             │  ← Bridges Spring AI & MCP
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│ GitHub MCP Server (via STDIO protocol)  │  ← npx @github/mcp-server
└─────────────────────────────────────────┘
```

---

## Key Features

- ✅ Claude integrated with GitHub data via MCP
- ✅ Automatic tool invocation based on user queries
- ✅ Full GitHub repository and issue search capabilities
- ✅ REST API for easy integration
- ✅ Comprehensive error handling and logging
- ✅ Health check endpoint

---

## Next Steps

1. Once the application is running, test various queries:
   - "Search for issues labeled 'bug' in spring-projects/spring-ai"
   - "List the latest pull requests in microsoft/vscode"
   - "Find JavaScript repositories trending this week"

2. Explore the ChatController to add more endpoints

3. Check logs for detailed MCP tool discovery and invocation

---

## Support

If you encounter issues:
1. Check logs for error messages: `logging.level.org.springframework.ai=DEBUG`
2. Verify all environment variables are set correctly
3. Test GitHub MCP server directly: `npm exec -- @github/mcp-server`
4. Check that npm packages are installed: `npm list -g @github/mcp-server`