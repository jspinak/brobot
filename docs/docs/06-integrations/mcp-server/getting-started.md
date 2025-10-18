---
sidebar_position: 1
---

# Getting Started with MCP Server

:::danger EXPERIMENTAL FEATURE - USE WITH CAUTION

**The MCP Server is an experimental, untested proof-of-concept.** This feature is provided for research, prototyping, and exploration purposes only.

**DO NOT use in production environments.**

**Key Warnings**:
- No comprehensive testing has been performed
- APIs and interfaces may change without notice
- Security vulnerabilities may exist
- Performance and stability not guaranteed
- Limited error handling and recovery mechanisms
- Documentation may not reflect current implementation

**By using this feature, you acknowledge these risks and agree to use it for experimental purposes only.**

:::

The Model Context Protocol (MCP) Server enables AI agents to control Brobot automations through a RESTful API. This guide will help you set up and start experimenting with the MCP server.

## What is MCP Server?

The MCP Server acts as a bridge between AI systems and Brobot's automation capabilities:

```
AI Agent (GPT-4, Claude) → MCP Server → Brobot → Your Application
```

This enables:
- 🤖 **AI-Driven Control**: AI agents can control automation through natural language
- 👁️ **Visual Feedback**: Brobot provides screenshots for AI decision-making
- 🔄 **State-Based Automation**: Leverage [Brobot's reliable state management](../../01-getting-started/states.md)
- 🚀 **Request Handling**: Process automation requests through REST API

:::info STATUS: PROOF OF CONCEPT
This integration is a **proof of concept** demonstrating how AI agents could control GUI automation. It has **not been tested** in real-world production scenarios.
:::

## Repository Structure

The MCP server repository is well-organized with the following structure:

```
brobot-mcp-server/
├── mcp_server/          # FastAPI server implementation
├── brobot-cli/          # Java CLI bridge to Brobot
├── brobot_client/       # Python client library (sync + async)
├── examples/            # Working code examples
│   └── example_client.py
├── tests/               # Test suite with pytest
├── docs/                # Additional documentation
├── .env.example         # Configuration template
├── docker-compose.yml   # Docker deployment
├── Dockerfile           # Container image
└── README.md            # Project overview
```

## Prerequisites

Before starting, ensure you have:

- ✅ Python 3.8 or higher
- ✅ Java 11 or higher
- ✅ [Brobot framework installed](../../01-getting-started/installation.md)
- ✅ A GUI application to automate
- ⚠️ **Willingness to experiment with untested software**

## Quick Start

:::caution
The installation and usage instructions below are provided as-is. You may encounter issues not covered in this documentation.
:::

### 1. Install MCP Server

```bash
# Clone the repository
git clone https://github.com/jspinak/brobot-mcp-server.git
cd brobot-mcp-server

# Install Python dependencies
pip install -e .
```

:::tip ALTERNATIVE INSTALLATION
You can also install the package directly from the command line after cloning:
```bash
pip install -e ".[dev]"  # Include development dependencies
```
:::

### 2. Build the CLI Bridge

```bash
# Build the Java CLI that connects to Brobot
cd brobot-cli
gradle shadowJar
cd ..
```

### 3. Configure the Server

Create a `.env` file (copy from `.env.example`):

```env
# Server Configuration
MCP_HOST=0.0.0.0
MCP_PORT=8000
MCP_LOG_LEVEL=info

# Brobot CLI Configuration
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar
JAVA_EXECUTABLE=java
CLI_TIMEOUT=30.0

# Use mock data (set to false for real Brobot integration)
USE_MOCK_DATA=false
```

:::tip CONFIGURATION
The server includes a `.env.example` file with all available configuration options. Copy it to `.env` and adjust as needed:
```bash
cp .env.example .env
```
:::

### 4. Start the Server

Start the server using either method:

**Method 1: Python module**
```bash
python -m mcp_server.main
```

**Method 2: Installed command** (after pip install)
```bash
brobot-mcp-server
```

The server will start at `http://localhost:8000`.

Visit `http://localhost:8000/docs` for interactive API documentation (Swagger UI) or `http://localhost:8000/redoc` for alternative documentation.

### 5. Verify Installation

Check that the server is running correctly:

```bash
# Check health endpoint
curl http://localhost:8000/health

# Expected response:
# {"status": "ok", "brobot_connected": false, "version": "0.1.0"}
```

:::tip DOCKER DEPLOYMENT
For containerized deployment, the repository includes Docker and Docker Compose configurations:
```bash
# Build and run with Docker Compose
docker-compose up

# Or build Docker image manually
docker build -t brobot-mcp-server .
docker run -p 8000:8000 brobot-mcp-server
```
:::

:::note
Expect potential startup errors, missing dependencies, or configuration issues as this is experimental software.
:::

## Your First AI Automation

:::info WORKING EXAMPLES
The MCP server repository includes working example code in the `examples/` directory:
- `examples/example_client.py` - Complete working example using direct API calls
- `brobot_client/examples/basic_usage.py` - Client library basic usage
- `brobot_client/examples/async_example.py` - Asynchronous client usage

See the [examples directory](https://github.com/jspinak/brobot-mcp-server/tree/main/examples) for complete, tested code.
:::

### Using Python Client

Install the client library from source:

```bash
# From the brobot-mcp-server repository
cd brobot_client
pip install -e .
```

:::info CLIENT PACKAGE
The `brobot-client` package is included in the MCP server repository under the `brobot_client/` directory. It provides both synchronous and asynchronous client interfaces.
:::

Create a simple automation using the client library:

```python
from brobot_client import BrobotClient

# Use context manager for automatic cleanup
with BrobotClient(base_url="http://localhost:8000") as client:
    # Get current observation
    observation = client.get_observation()
    print(f"Active states: {[s.name for s in observation.active_states]}")

    # Perform actions
    result = client.click(image_pattern="login_button.png", confidence=0.9)
    print(f"Click successful: {result.success}")

    client.type_text("username@example.com")
    client.click("submit_button.png")

    # Wait for state transition
    client.wait_for_state("dashboard", timeout=30.0)
```

For AI integration with the client:

```python
from brobot_client import BrobotClient
from openai import OpenAI

# Initialize clients
with BrobotClient() as brobot:
    client = OpenAI(api_key="your-api-key")

    # Get current screen state
    observation = brobot.get_observation()

    # Ask AI what to do
    response = client.chat.completions.create(
        model="gpt-4",
        messages=[{
            "role": "user",
            "content": f"I see these UI elements: {observation.active_states}. How do I log in?"
        }]
    )

    # Execute AI-suggested actions
    # (In production, parse AI response and execute appropriate actions)
    brobot.click("login_button.png")
    brobot.type_text("username@example.com")
    brobot.click("submit_button.png")
```

### Using Direct API Calls

```python
import requests

# Get current observation
response = requests.get("http://localhost:8000/api/v1/observation")
observation = response.json()

# Execute an action
action = {
    "action_type": "click",
    "parameters": {
        "image_pattern": "login_button.png"
    }
}
response = requests.post("http://localhost:8000/api/v1/execute", json=action)
```

## Operating Modes

The MCP server supports two operating modes:

### Mock Mode (Default)
- Uses hardcoded mock data for testing
- No Brobot CLI required
- Perfect for development and testing the API
- Set `USE_MOCK_DATA=true` in `.env`

### CLI Mode (Real Brobot Integration)
- Connects to actual Brobot automation via Java CLI
- Requires Brobot CLI JAR to be built
- Provides real GUI automation capabilities
- Set `USE_MOCK_DATA=false` in `.env`

:::tip GETTING STARTED
Start with **Mock Mode** to test the API and client library. Once comfortable, switch to **CLI Mode** for real automation.
:::

## Core Concepts

### States and Observations

[Brobot uses a state-based approach](../../01-getting-started/core-concepts.md). The MCP server exposes:

- **State Structure**: The complete map of your application's states
- **Observations**: Current state with screenshot and confidence scores

### Actions

Available actions mirror [Brobot's action capabilities](../../01-getting-started/action-hierarchy.md):

- `click`: Click on UI elements
- `type`: Enter text
- `drag`: Drag and drop
- `find`: Locate UI elements
- `vanish`: Wait for elements to disappear

Learn more about [Brobot's state transitions](../../01-getting-started/transitions.md) and how actions connect states.

### Integration Patterns

Common patterns for AI integration:

1. **Autonomous Agent**: AI observes and acts independently
2. **Guided Automation**: AI suggests actions for approval
3. **Hybrid Control**: Mix manual and AI-driven steps

## Next Steps

:::warning
All linked documentation is also experimental and may contain inaccuracies or outdated information.
:::

- 📖 Read the [API Reference](./api-reference.md) for detailed endpoint documentation
- 🔧 Learn about [Configuration Options](./configuration.md)
- 💡 Explore [AI Integration Examples](./examples.md)
- 🐛 Check [Troubleshooting Guide](./troubleshooting.md) if you encounter issues

## Getting Help

:::info COMMUNITY SUPPORT ONLY
Support for this experimental feature is limited to community contributions. There are no guarantees of responses or fixes.
:::

- **GitHub Issues**: [Report bugs or request features](https://github.com/jspinak/brobot-mcp-server/issues)
- **Documentation**: Full docs at [brobot.dev](https://brobot.dev)

## Feedback Welcome

**We need your feedback to improve this feature!** If you experiment with the MCP server:

- Report any bugs or issues you encounter
- Share your use cases and integration patterns
- Suggest API improvements
- Contribute code, tests, or documentation
- Help us understand where this feature adds value

Your feedback will help determine whether this feature should be developed further, improved, or deprecated.
