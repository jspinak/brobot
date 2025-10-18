---
sidebar_position: 3
---

# Configuration Guide

:::warning EXPERIMENTAL FEATURE

**This is experimental software with limited configuration options.** The MCP Server currently supports 9 basic configuration settings. Advanced features like security, logging, and performance tuning are planned for future releases.

**Configuration options may change in future versions.**

:::

Learn how to configure the MCP Server for different environments and use cases.

## Quick Start

The simplest way to configure the server:

```bash
# Copy the example configuration
cp .env.example .env

# Edit with your settings
nano .env
```

## Available Configuration Options

The MCP Server supports **9 configuration options** via environment variables or `.env` file.

### Server Configuration

```env
# Server host and port
MCP_HOST=0.0.0.0              # Bind address (default: 0.0.0.0)
MCP_PORT=8000                 # Server port (default: 8000)

# Development settings
MCP_RELOAD=true               # Auto-reload on code changes (default: true)
MCP_LOG_LEVEL=info            # Log level: debug|info|warning|error|critical (default: info)

# API versioning
API_VERSION=v1                # API version prefix (default: v1)
```

### Brobot CLI Configuration

```env
# CLI JAR location
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar  # Path to CLI JAR (auto-discovered if not set)

# Java configuration
JAVA_EXECUTABLE=java          # Path to java executable (default: java)

# CLI behavior
CLI_TIMEOUT=30.0              # Command timeout in seconds (default: 30.0)

# Mock mode
USE_MOCK_DATA=true            # Use mock data instead of real CLI (default: true)
```

## Configuration Methods

The MCP Server loads configuration in this order (highest to lowest precedence):

1. **Environment Variables**: Set in your shell or CI/CD environment
2. **`.env` File**: Local configuration file in project root
3. **Default Values**: Built-in defaults from the Settings class

Example of precedence:
```bash
# Set in .env file
MCP_PORT=8000

# Override with environment variable
MCP_PORT=9000 python -m mcp_server.main  # Uses port 9000
```

## Environment-Specific Configurations

### Development

Recommended settings for local development:

```env
# .env.development
MCP_HOST=localhost
MCP_PORT=8000
MCP_RELOAD=true
MCP_LOG_LEVEL=debug
USE_MOCK_DATA=true
```

**Start server:**
```bash
cp .env.development .env
python -m mcp_server.main
```

### Production

Recommended settings for production deployment:

```env
# .env.production
MCP_HOST=0.0.0.0
MCP_PORT=8000
MCP_RELOAD=false
MCP_LOG_LEVEL=warning
USE_MOCK_DATA=false
BROBOT_CLI_JAR=/app/brobot-cli.jar
CLI_TIMEOUT=60.0
```

**Start server:**
```bash
cp .env.production .env
python -m mcp_server.main
```

### Testing

Recommended settings for automated testing:

```env
# .env.test
MCP_PORT=8001
MCP_LOG_LEVEL=info
USE_MOCK_DATA=true
CLI_TIMEOUT=5.0
```

**Run tests:**
```bash
cp .env.test .env
pytest
```

## Configuration Examples

### Minimal Configuration

The absolute minimum to get started:

```env
# Just use mock mode (default settings for everything else)
USE_MOCK_DATA=true
```

### CLI Mode Configuration

To use real Brobot automation (not mock mode):

```env
# Essential settings for CLI mode
USE_MOCK_DATA=false
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar
JAVA_EXECUTABLE=/usr/bin/java
CLI_TIMEOUT=60.0
```

### Custom Port and Host

To run on a different address/port:

```env
# Custom network settings
MCP_HOST=127.0.0.1
MCP_PORT=9000
USE_MOCK_DATA=true
```

### Debug Mode

For troubleshooting and development:

```env
# Maximum verbosity
MCP_LOG_LEVEL=debug
MCP_RELOAD=true
USE_MOCK_DATA=true
```

## Programmatic Access

Access configuration in Python code:

```python
from mcp_server.config import get_settings

settings = get_settings()

# Access configuration values
print(f"Server running on {settings.host}:{settings.port}")
print(f"Mock mode: {settings.use_mock_data}")
print(f"CLI JAR: {settings.brobot_cli_jar}")
print(f"Log level: {settings.log_level}")

# Export all settings
config_dict = settings.model_dump()
print(config_dict)
```

## Configuration Validation

### Check Current Configuration

View the loaded configuration:

```python
from mcp_server.config import get_settings

settings = get_settings()
print(settings.model_dump())
```

### Verify Environment Variables

Check which environment variables are set:

```bash
# View all MCP_ environment variables
printenv | grep MCP_

# View specific variable
echo $MCP_PORT
```

### Test Configuration

Test your configuration without starting the server:

```python
from mcp_server.config import get_settings

try:
    settings = get_settings()
    print("✓ Configuration loaded successfully")
    print(f"✓ Server will start on {settings.host}:{settings.port}")
    print(f"✓ Mock mode: {settings.use_mock_data}")
except Exception as e:
    print(f"✗ Configuration error: {e}")
```

## Common Configuration Patterns

### Docker Deployment

When running in Docker, use environment variables:

```bash
docker run -d \
  -p 8000:8000 \
  -e USE_MOCK_DATA=false \
  -e BROBOT_CLI_JAR=/app/brobot-cli.jar \
  -e MCP_LOG_LEVEL=info \
  brobot-mcp-server
```

Or use docker-compose with `.env` file:

```yaml
# docker-compose.yml
version: '3.8'
services:
  brobot-mcp:
    image: brobot-mcp-server
    ports:
      - "8000:8000"
    env_file:
      - .env
```

### Multiple Instances

Run multiple instances on different ports:

```bash
# Instance 1 (mock mode)
MCP_PORT=8000 USE_MOCK_DATA=true python -m mcp_server.main &

# Instance 2 (CLI mode)
MCP_PORT=8001 USE_MOCK_DATA=false python -m mcp_server.main &
```

### CI/CD Pipeline

Set configuration via environment variables:

```yaml
# .github/workflows/test.yml
env:
  MCP_PORT: 8000
  USE_MOCK_DATA: true
  MCP_LOG_LEVEL: debug
```

## Best Practices

### Security

1. **Never commit `.env` files** with sensitive configurations to version control
2. **Use environment variables** for production secrets and credentials
3. **Add `.env` to `.gitignore`** to prevent accidental commits
4. **Use separate configurations** for development, staging, and production

### Performance

1. **Disable auto-reload** (`MCP_RELOAD=false`) in production for better performance
2. **Adjust CLI timeout** based on your automation complexity
3. **Use appropriate log levels** (warning or error in production, debug in development)
4. **Monitor resource usage** and adjust timeout values accordingly

### Development

1. **Use mock mode** (`USE_MOCK_DATA=true`) for faster development iteration
2. **Enable debug logging** (`MCP_LOG_LEVEL=debug`) when troubleshooting
3. **Use auto-reload** (`MCP_RELOAD=true`) for automatic code updates
4. **Keep example configurations** in version control (`.env.example`, `.env.development`, etc.)

## Troubleshooting Configuration

### Configuration Not Loading

**Issue**: Changes to `.env` not taking effect

**Solution**: Restart the server - configuration is loaded once at startup
```bash
# Stop the server (Ctrl+C)
# Start again
python -m mcp_server.main
```

### Environment Variables Not Working

**Issue**: Environment variables not overriding `.env` file

**Solution**: Verify environment variables are set correctly
```bash
# Check if variable is set
echo $MCP_PORT

# Set and run in one command
MCP_PORT=9000 python -m mcp_server.main
```

### CLI JAR Not Found

**Issue**: `BROBOT_CLI_JAR` path is incorrect

**Solution**: Use absolute path or verify relative path
```bash
# Check if file exists
ls -la brobot-cli/build/libs/brobot-cli.jar

# Use absolute path
BROBOT_CLI_JAR=/full/path/to/brobot-cli.jar python -m mcp_server.main
```

### Port Already in Use

**Issue**: `Address already in use` error

**Solution**: Change the port or stop the conflicting process
```bash
# Find process using port
lsof -i :8000

# Use different port
MCP_PORT=8001 python -m mcp_server.main
```

## Future Configuration Options

The following features are **planned for future releases** and are not currently implemented:

### Planned Features

- **Security**: API key authentication, rate limiting, SSL/TLS support
- **Logging**: File logging with rotation, structured logging, log filtering
- **Performance**: Response caching, pattern caching, worker processes
- **Advanced Server**: CORS configuration, request limits, connection pooling
- **Brobot Framework**: Direct configuration of Brobot action timings and thresholds

See the [project roadmap](https://github.com/jspinak/brobot-mcp-server/issues) for planned features and their status.

## Next Steps

After configuring your MCP server:

- 📖 Read the [API Reference](./api-reference.md) for detailed endpoint documentation
- 💡 Explore [Examples](./examples.md) for different configuration use cases
- 🐛 Check [Troubleshooting](./troubleshooting.md) if you encounter issues

If you haven't completed installation yet:

- 📦 Review the [Installation Guide](./installation.md) for setup instructions
- 🚀 Follow the [Getting Started](./getting-started.md) tutorial for quick start
