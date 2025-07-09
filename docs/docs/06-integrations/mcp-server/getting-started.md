---
sidebar_position: 1
---

# Getting Started with MCP Server

The Model Context Protocol (MCP) Server enables AI agents to control Brobot automations through a RESTful API. This guide will help you set up and start using the MCP server in minutes.

## What is MCP Server?

The MCP Server acts as a bridge between AI systems and Brobot's automation capabilities:

```
AI Agent (GPT-4, Claude) → MCP Server → Brobot → Your Application
```

This enables:
- 🤖 **Natural Language Control**: Tell AI what to do in plain English
- 👁️ **Visual Feedback**: AI can see screenshots and make decisions
- 🔄 **State-Based Automation**: Leverage Brobot's reliable state management
- 🚀 **Parallel Processing**: Handle multiple automation requests

## Prerequisites

Before starting, ensure you have:

- ✅ Python 3.8 or higher
- ✅ Java 11 or higher
- ✅ Brobot framework installed
- ✅ A GUI application to automate

## Quick Start

### 1. Install MCP Server

```bash
# Clone the repository
git clone https://github.com/jspinak/brobot-mcp-server.git
cd brobot-mcp-server

# Install Python dependencies
pip install -e .
```

### 2. Build the CLI Bridge

```bash
# Build the Java CLI that connects to Brobot
cd brobot-cli
gradle shadowJar
cd ..
```

### 3. Configure the Server

Create a `.env` file:

```env
# Disable mock mode to use real Brobot
USE_MOCK_DATA=false

# Path to the CLI JAR
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar
```

### 4. Start the Server

```bash
python -m mcp_server.main
```

The server is now running at `http://localhost:8000`! 

Visit `http://localhost:8000/docs` for interactive API documentation.

## Your First AI Automation

### Using Python Client

Install the client library:

```bash
pip install brobot-client
```

Create a simple automation:

```python
from brobot_client import BrobotClient
import openai

# Initialize clients
brobot = BrobotClient()
openai.api_key = "your-api-key"

# Get current screen state
observation = brobot.get_observation()

# Ask AI what to do
response = openai.ChatCompletion.create(
    model="gpt-4",
    messages=[{
        "role": "user",
        "content": f"I see these UI elements: {observation.active_states}. How do I log in?"
    }]
)

# AI might respond: "Click on the login button, enter credentials, then submit"
# Execute the suggested actions
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

## Core Concepts

### States and Observations

Brobot uses a state-based approach. The MCP server exposes:

- **State Structure**: The complete map of your application's states
- **Observations**: Current state with screenshot and confidence scores

### Actions

Available actions mirror Brobot's capabilities:

- `click`: Click on UI elements
- `type`: Enter text
- `drag`: Drag and drop
- `wait`: Wait for state changes

### Integration Patterns

Common patterns for AI integration:

1. **Autonomous Agent**: AI observes and acts independently
2. **Guided Automation**: AI suggests actions for approval
3. **Hybrid Control**: Mix manual and AI-driven steps

## Next Steps

- 📖 Read the [API Reference](./api-reference) for detailed endpoint documentation
- 🔧 Learn about [Configuration Options](./configuration)
- 💡 Explore [AI Integration Examples](./examples)
- 🐛 Check [Troubleshooting Guide](./troubleshooting) if you encounter issues

## Getting Help

- **GitHub Issues**: [Report bugs or request features](https://github.com/jspinak/brobot-mcp-server/issues)
- **Discord Community**: Join our Discord for real-time help
- **Documentation**: Full docs at [brobot.dev](https://brobot.dev)