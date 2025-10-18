---
sidebar_position: 2
---

# Installation Guide

:::warning EXPERIMENTAL FEATURE

**This installation guide is for an experimental feature.** While the software is functional, it is still under active development.

**Important Notes**:
- This is a proof-of-concept implementation
- APIs and interfaces may change without notice
- Limited testing has been performed
- Configuration options may evolve
- Community support only

**By following these instructions, you acknowledge the experimental nature of this software.**

:::

This guide provides detailed installation instructions for the [Brobot MCP Server](https://github.com/jspinak/brobot-mcp-server) on different operating systems.

## Prerequisites

Before installing the MCP Server, ensure you understand:

- **[Brobot Framework](../../01-getting-started/installation.md)**: The core automation library
- **[States and Transitions](../../01-getting-started/states.md)**: How Brobot models applications
- **[Core Concepts](../../01-getting-started/core-concepts.md)**: Brobot architecture overview

:::tip INSTALLATION ORDER
While not required, installing and understanding the [core Brobot framework](../../01-getting-started/installation.md) first will help you understand the MCP Server's capabilities.
:::

## System Requirements

### Minimum Requirements
- **CPU**: 2 cores
- **RAM**: 4 GB
- **Disk**: 500 MB free space
- **Display**: Required for GUI automation

### Recommended Requirements
- **CPU**: 4+ cores
- **RAM**: 8 GB
- **Disk**: 1 GB free space
- **Display**: 1920x1080 or higher resolution

### Software Prerequisites
- **Python**: 3.8 or higher (3.11+ recommended)
- **Java**: JDK 11 or higher (JDK 17 recommended for Docker)
- **Git**: For cloning the repository
- **Gradle**: 7.0+ (system installation required - no wrapper included)

## Platform-Specific Installation

### Windows

1. **Install Python**
   ```powershell
   # Download from python.org or use winget
   winget install Python.Python.3.11
   
   # Verify installation
   python --version
   ```

2. **Install Java**
   ```powershell
   # Using winget
   winget install Microsoft.OpenJDK.11
   
   # Or download from adoptium.net
   # Set JAVA_HOME environment variable
   setx JAVA_HOME "C:\Program Files\Microsoft\jdk-11.0.x.xxx"
   ```

3. **Clone and Install MCP Server**
   ```powershell
   git clone https://github.com/jspinak/brobot-mcp-server.git
   cd brobot-mcp-server
   
   # Create virtual environment
   python -m venv venv
   .\venv\Scripts\activate
   
   # Install server
   pip install -e .
   ```

4. **Build Brobot CLI**
   ```powershell
   cd brobot-cli
   gradle shadowJar
   cd ..
   ```

   :::note
   The project does not include Gradle wrapper files. Ensure Gradle is installed system-wide.
   :::

### macOS

1. **Install Prerequisites via Homebrew**
   ```bash
   # Install Homebrew if not present
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   
   # Install Python and Java
   brew install python@3.11 openjdk@11
   
   # Add Java to PATH
   echo 'export PATH="/usr/local/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

2. **Clone and Install MCP Server**
   ```bash
   git clone https://github.com/jspinak/brobot-mcp-server.git
   cd brobot-mcp-server
   
   # Create virtual environment
   python3 -m venv venv
   source venv/bin/activate
   
   # Install server
   pip install -e .
   ```

3. **Build Brobot CLI**
   ```bash
   cd brobot-cli
   gradle shadowJar
   cd ..
   ```

### Linux (Ubuntu/Debian)

1. **Install System Packages**
   ```bash
   # Update package list
   sudo apt update
   
   # Install Python and dependencies
   sudo apt install python3.11 python3.11-venv python3-pip
   
   # Install Java
   sudo apt install openjdk-11-jdk
   
   # Install additional tools
   sudo apt install git curl wget
   ```

2. **Clone and Install MCP Server**
   ```bash
   git clone https://github.com/jspinak/brobot-mcp-server.git
   cd brobot-mcp-server
   
   # Create virtual environment
   python3.11 -m venv venv
   source venv/bin/activate
   
   # Install server
   pip install -e .
   ```

3. **Build Brobot CLI**
   ```bash
   cd brobot-cli
   gradle shadowJar
   cd ..
   ```

## Docker Installation

The repository includes production-ready Docker and Docker Compose configurations.

### Using Docker Compose (Recommended)

1. **Basic Setup**
   ```bash
   # Clone the repository
   git clone https://github.com/jspinak/brobot-mcp-server.git
   cd brobot-mcp-server

   # Start the server
   docker-compose up -d
   ```

2. **Development Mode with Hot Reload**
   ```bash
   docker-compose --profile dev up brobot-mcp-dev
   ```

3. **With Optional Services**
   ```bash
   # With Redis caching
   docker-compose --profile with-redis up -d

   # With PostgreSQL
   docker-compose --profile with-postgres up -d
   ```

4. **Check Status**
   ```bash
   docker-compose ps
   docker-compose logs -f brobot-mcp-server
   ```

### Using Docker Directly

1. **Build Image**
   ```bash
   docker build -t brobot-mcp-server .
   ```

2. **Run Container**
   ```bash
   docker run -d \
     -p 8000:8000 \
     -e USE_MOCK_DATA=false \
     -v $(pwd)/logs:/app/logs \
     -v $(pwd)/data:/app/data \
     --name brobot-mcp \
     brobot-mcp-server
   ```

3. **View Logs**
   ```bash
   docker logs -f brobot-mcp
   ```

:::tip DOCKERFILE
The repository includes a multi-stage Dockerfile that:
- Builds the Java CLI with Gradle 8 + JDK 17
- Uses Python 3.13 runtime with Java 17 JRE
- Includes health checks and proper user permissions
- See [`Dockerfile`](https://github.com/jspinak/brobot-mcp-server/blob/main/Dockerfile) for details
:::

## Verifying Installation

### 1. Check Python Installation
```bash
python --version
# Should show: Python 3.8.x or higher

pip --version
# Should show pip version
```

### 2. Check Java Installation
```bash
java -version
# Should show: openjdk version "11.x.x" or higher

javac -version
# Should show: javac 11.x.x
```

### 3. Test MCP Server
```bash
# Start server in mock mode
USE_MOCK_DATA=true python -m mcp_server.main

# In another terminal, test health endpoint
curl http://localhost:8000/health
# Should return: {"status":"ok"}
```

### 4. Test Brobot CLI
```bash
java -jar brobot-cli/build/libs/brobot-cli.jar --version
# Should show: brobot-cli 0.1.0
```

## Installing the Python Client

The Python client library is included in the repository but not yet published to PyPI.

**Install from source:**

```bash
# From the brobot-mcp-server repository
cd brobot_client
pip install -e .
```

**With development dependencies:**

```bash
cd brobot_client
pip install -e ".[dev]"
```

**Verify installation:**

```python
from brobot_client import BrobotClient
print("Client library installed successfully!")
```

:::info PACKAGE LOCATION
The `brobot-client` package is located in the `brobot_client/` subdirectory of the MCP server repository.
:::

## Configuration

### Basic Configuration

The repository includes a `.env.example` file with all configuration options. Copy and customize it:

```bash
cp .env.example .env
```

**Configuration options:**

```env
# Server Configuration
MCP_HOST=0.0.0.0
MCP_PORT=8000
MCP_RELOAD=true
MCP_LOG_LEVEL=info

# Brobot CLI Configuration
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar
JAVA_EXECUTABLE=java
CLI_TIMEOUT=30.0

# Use mock data (set to false for real Brobot integration)
USE_MOCK_DATA=true

# API Configuration
API_VERSION=v1
```

:::tip CONFIGURATION FILE
The `.env.example` file includes sensible defaults. Start with mock mode (`USE_MOCK_DATA=true`) to test the API, then switch to CLI mode (`USE_MOCK_DATA=false`) for real automation.
:::

### Performance Tuning

Adjust the CLI timeout for slower systems or complex automations:

```env
# CLI timeout in seconds (default: 30.0)
CLI_TIMEOUT=60.0
```

:::info FUTURE FEATURES
Authentication, metrics, and worker configuration are planned for future releases. The current implementation supports the configuration options listed in the Basic Configuration section above.
:::

## Platform-Specific Considerations

### WSL (Windows Subsystem for Linux)

GUI automation in WSL requires additional setup:

```bash
# Option 1: Use mock mode for testing (recommended)
echo "USE_MOCK_DATA=true" >> .env

# Option 2: Set up X server for GUI operations
# Install X server on Windows (VcXsrv, X410, etc.)
export DISPLAY=:0
echo 'export DISPLAY=:0' >> ~/.bashrc
```

### Headless Servers

For servers without displays:

```bash
# Option 1: Use mock mode
USE_MOCK_DATA=true python -m mcp_server.main

# Option 2: Install virtual display (Xvfb)
sudo apt install xvfb
xvfb-run python -m mcp_server.main
```

:::warning DISPLAY REQUIREMENT
GUI automation requires a display server. For testing without a display, use mock mode (`USE_MOCK_DATA=true` in `.env`).
:::

## Troubleshooting Installation

### Python Issues

**Issue**: `pip: command not found`
```bash
# Install pip
python -m ensurepip --upgrade
```

**Issue**: `No module named 'venv'`
```bash
# Install venv package
sudo apt install python3.11-venv  # Ubuntu/Debian
```

### Java Issues

**Issue**: `JAVA_HOME not set`
```bash
# Find Java installation
which java

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
echo 'export JAVA_HOME=/usr/lib/jvm/java-11-openjdk' >> ~/.bashrc
```

**Issue**: Gradle not found
```bash
# Install Gradle system-wide (the project does not include wrapper files)
# Ubuntu/Debian
sudo apt install gradle

# macOS
brew install gradle

# Windows
winget install Gradle.Gradle
```

### Network Issues

**Issue**: Port 8000 already in use
```bash
# Find process using port
lsof -i :8000  # Unix
netstat -ano | findstr :8000  # Windows

# Use different port
MCP_PORT=8080 python -m mcp_server.main
```

## Next Steps

After successful installation:

1. Read the [Configuration Guide](./configuration.md) for detailed setup options
2. Follow the [Getting Started](./getting-started.md) tutorial
3. Explore [API Examples](./examples.md) for integration patterns
4. Review [API Reference](./api-reference.md) for endpoint documentation

## Getting Help

If you encounter issues:

:::info COMMUNITY SUPPORT ONLY
Support for this experimental feature is limited to community contributions. There are no guarantees of responses or fixes.
:::

1. Check the [Troubleshooting Guide](./troubleshooting.md)
2. Search [GitHub Issues](https://github.com/jspinak/brobot-mcp-server/issues)
3. Create a new issue with installation logs
4. Review the main [Brobot documentation](../../01-getting-started/installation.md) for framework prerequisites