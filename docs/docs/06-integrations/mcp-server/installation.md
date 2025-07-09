---
sidebar_position: 2
---

# Installation Guide

This guide provides detailed installation instructions for the Brobot MCP Server on different operating systems.

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
- **Python**: 3.8 or higher
- **Java**: JDK 11 or higher
- **Git**: For cloning the repository
- **Gradle**: 7.0+ (or use included wrapper)

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
   .\gradlew.bat shadowJar
   cd ..
   ```

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
   ./gradlew shadowJar
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
   ./gradlew shadowJar
   cd ..
   ```

## Docker Installation

For a containerized setup:

1. **Create Dockerfile**
   ```dockerfile
   FROM python:3.11-slim
   
   # Install Java
   RUN apt-get update && \
       apt-get install -y openjdk-11-jdk && \
       apt-get clean
   
   # Copy application
   WORKDIR /app
   COPY . /app/
   
   # Install Python dependencies
   RUN pip install -e .
   
   # Build Java CLI
   RUN cd brobot-cli && ./gradlew shadowJar
   
   # Expose port
   EXPOSE 8000
   
   # Start server
   CMD ["python", "-m", "mcp_server.main"]
   ```

2. **Build and Run**
   ```bash
   docker build -t brobot-mcp-server .
   docker run -p 8000:8000 brobot-mcp-server
   ```

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

For Python applications:

```bash
pip install brobot-client
```

Or from source:

```bash
cd brobot_client
pip install -e .
```

## Configuration

### Basic Configuration

Create a `.env` file in the project root:

```env
# Server Settings
MCP_HOST=0.0.0.0
MCP_PORT=8000

# Brobot CLI
USE_MOCK_DATA=false
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar

# Logging
MCP_LOG_LEVEL=info
```

### Advanced Configuration

For production deployments:

```env
# Performance
CLI_TIMEOUT=60.0
WORKERS=4

# Security (future)
API_KEY_REQUIRED=true
API_KEY=your-secret-key

# Monitoring
ENABLE_METRICS=true
METRICS_PORT=9090
```

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
# Use the Gradle wrapper instead
./gradlew shadowJar  # Unix
gradlew.bat shadowJar  # Windows
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

1. Read the [Configuration Guide](./configuration) for detailed setup options
2. Follow the [Getting Started](./getting-started) tutorial
3. Explore [API Examples](./examples) for integration patterns

## Getting Help

If you encounter issues:

1. Check the [Troubleshooting Guide](./troubleshooting)
2. Search [GitHub Issues](https://github.com/jspinak/brobot-mcp-server/issues)
3. Ask on [Discord](https://discord.gg/brobot)
4. Create a new issue with installation logs