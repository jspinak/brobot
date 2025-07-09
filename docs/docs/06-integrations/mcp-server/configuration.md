---
sidebar_position: 3
---

# Configuration Guide

Learn how to configure the MCP Server for different environments and use cases.

## Configuration Methods

The MCP Server supports multiple configuration methods, in order of precedence:

1. **Environment Variables**: Highest priority
2. **`.env` File**: Local configuration
3. **Default Values**: Built-in defaults

## Server Configuration

### Basic Settings

```env
# Server host and port
MCP_HOST=0.0.0.0              # Bind address (default: 0.0.0.0)
MCP_PORT=8000                 # Server port (default: 8000)

# Development settings
MCP_RELOAD=true               # Auto-reload on code changes (default: true)
MCP_LOG_LEVEL=info           # Log level: debug|info|warning|error (default: info)

# API versioning
API_VERSION=v1               # API version prefix (default: v1)
```

### Advanced Server Settings

```env
# Performance tuning
WORKERS=4                    # Number of worker processes (production only)
WORKER_CONNECTIONS=1000      # Max connections per worker
KEEPALIVE=5                  # Keep-alive timeout in seconds

# Request limits
MAX_REQUEST_SIZE=10485760    # Max request size in bytes (10MB)
REQUEST_TIMEOUT=300          # Request timeout in seconds

# CORS settings
CORS_ORIGINS=*              # Allowed origins (* for all)
CORS_METHODS=GET,POST       # Allowed HTTP methods
CORS_HEADERS=*              # Allowed headers
```

## Brobot CLI Configuration

### CLI Path Settings

```env
# CLI JAR location
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar

# Custom Java executable
JAVA_EXECUTABLE=java         # Path to java executable
JAVA_OPTS=-Xmx2G -Xms512M  # JVM options

# CLI behavior
CLI_TIMEOUT=30.0            # Command timeout in seconds
CLI_RETRY_ATTEMPTS=3        # Retry failed commands
CLI_RETRY_DELAY=1.0        # Delay between retries
```

### Mock Mode

```env
# Enable/disable mock mode
USE_MOCK_DATA=true          # Use mock data instead of real CLI

# Mock data settings (when USE_MOCK_DATA=true)
MOCK_DELAY=0.5              # Simulate action delays
MOCK_SUCCESS_RATE=0.95      # Probability of action success
MOCK_STATE_COUNT=5          # Number of mock states
```

## Brobot Framework Configuration

### Image Recognition

```env
# Pattern matching settings
BROBOT_MIN_SIMILARITY=0.8    # Minimum similarity for matches (0.0-1.0)
BROBOT_SEARCH_TIMEOUT=5.0    # Timeout for finding patterns
BROBOT_MULTI_MATCHES=false   # Allow multiple matches

# Screenshot settings
BROBOT_SCREENSHOT_FORMAT=png  # Screenshot format: png|jpg
BROBOT_SCREENSHOT_QUALITY=90  # JPEG quality (1-100)
```

### Action Configuration

```env
# Click actions
BROBOT_CLICK_DELAY=0.1       # Delay after click (seconds)
BROBOT_DOUBLE_CLICK_INTERVAL=0.3  # Time between double clicks

# Typing actions
BROBOT_TYPING_SPEED=300      # Characters per minute
BROBOT_TYPING_DELAY=0.05     # Delay between keystrokes

# Drag actions
BROBOT_DRAG_SPEED=1000       # Pixels per second
BROBOT_DRAG_SMOOTHNESS=10    # Movement steps (higher = smoother)
```

### State Management

```env
# State detection
BROBOT_STATE_TIMEOUT=10.0    # Max time to detect state
BROBOT_STATE_CHECK_INTERVAL=0.5  # Interval between checks

# State transition
BROBOT_TRANSITION_TIMEOUT=30.0   # Max time for state transition
BROBOT_TRANSITION_STABILITY=2.0  # Time state must be stable
```

## Security Configuration

### Authentication (Future Release)

```env
# API key authentication
API_KEY_REQUIRED=false       # Require API key
API_KEY_HEADER=X-API-Key    # Header name for API key

# Allowed API keys (comma-separated)
API_KEYS=key1,key2,key3

# Rate limiting
RATE_LIMIT_ENABLED=true      # Enable rate limiting
RATE_LIMIT_REQUESTS=100      # Requests per window
RATE_LIMIT_WINDOW=60         # Window size in seconds
```

### Network Security

```env
# Allowed hosts
ALLOWED_HOSTS=localhost,127.0.0.1,example.com

# SSL/TLS (when using HTTPS)
SSL_CERT_FILE=/path/to/cert.pem
SSL_KEY_FILE=/path/to/key.pem
SSL_VERIFY=true              # Verify SSL certificates
```

## Logging Configuration

### Log Settings

```env
# Log output
LOG_TO_FILE=true             # Enable file logging
LOG_FILE_PATH=logs/mcp.log   # Log file location
LOG_FILE_ROTATION=daily      # Rotation: daily|size|time
LOG_FILE_MAX_SIZE=10MB       # Max size before rotation
LOG_FILE_BACKUP_COUNT=7      # Number of backup files

# Log format
LOG_FORMAT=json              # Format: json|text
LOG_INCLUDE_TIMESTAMP=true   # Include timestamps
LOG_INCLUDE_HOSTNAME=true    # Include hostname
```

### Debug Settings

```env
# Debug options
DEBUG_MODE=false             # Enable debug mode
DEBUG_SQL=false              # Log SQL queries (if using DB)
DEBUG_REQUESTS=true          # Log all HTTP requests
DEBUG_RESPONSES=false        # Log all HTTP responses
DEBUG_BROBOT_COMMANDS=true   # Log CLI commands
```

## Performance Optimization

### Caching

```env
# Response caching
CACHE_ENABLED=true           # Enable response caching
CACHE_TTL=300               # Cache TTL in seconds
CACHE_MAX_SIZE=100MB        # Max cache size

# Pattern caching
PATTERN_CACHE_ENABLED=true   # Cache image patterns
PATTERN_CACHE_DIR=.cache/patterns
```

### Resource Limits

```env
# Memory limits
MAX_MEMORY_PERCENT=80        # Max memory usage percentage
GC_THRESHOLD=75              # Trigger GC at this memory %

# Connection limits
MAX_CONNECTIONS=1000         # Max concurrent connections
CONNECTION_TIMEOUT=30        # Connection timeout seconds
```

## Environment-Specific Configurations

### Development

```env
# .env.development
MCP_HOST=localhost
MCP_PORT=8000
MCP_RELOAD=true
MCP_LOG_LEVEL=debug
USE_MOCK_DATA=true
DEBUG_MODE=true
```

### Production

```env
# .env.production
MCP_HOST=0.0.0.0
MCP_PORT=80
MCP_RELOAD=false
MCP_LOG_LEVEL=warning
USE_MOCK_DATA=false
WORKERS=4
API_KEY_REQUIRED=true
RATE_LIMIT_ENABLED=true
LOG_TO_FILE=true
```

### Testing

```env
# .env.test
MCP_PORT=8001
MCP_LOG_LEVEL=info
USE_MOCK_DATA=true
MOCK_SUCCESS_RATE=1.0
CLI_TIMEOUT=5.0
```

## Configuration File Examples

### Minimal Configuration

```env
# Minimal .env for getting started
USE_MOCK_DATA=false
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar
```

### Full Development Configuration

```env
# Complete development configuration
# Server
MCP_HOST=0.0.0.0
MCP_PORT=8000
MCP_RELOAD=true
MCP_LOG_LEVEL=debug

# Brobot CLI
USE_MOCK_DATA=false
BROBOT_CLI_JAR=brobot-cli/build/libs/brobot-cli.jar
JAVA_EXECUTABLE=/usr/bin/java
JAVA_OPTS=-Xmx1G
CLI_TIMEOUT=30.0

# Brobot Framework
BROBOT_MIN_SIMILARITY=0.85
BROBOT_SEARCH_TIMEOUT=10.0
BROBOT_TYPING_SPEED=200

# Logging
LOG_TO_FILE=true
LOG_FILE_PATH=logs/dev.log
DEBUG_MODE=true
DEBUG_BROBOT_COMMANDS=true

# Performance
CACHE_ENABLED=true
PATTERN_CACHE_ENABLED=true
```

## Loading Configuration

### Automatic Loading

The server automatically loads configuration in this order:

1. System environment variables
2. `.env` file in project root
3. Built-in defaults

### Programmatic Access

Access configuration in Python code:

```python
from mcp_server.config import get_settings

settings = get_settings()
print(f"Server running on port: {settings.port}")
print(f"Mock mode: {settings.use_mock_data}")
```

### Runtime Validation

The server validates configuration on startup:

```bash
# Test configuration without starting server
python -m mcp_server.config --validate

# Show current configuration
python -m mcp_server.config --show
```

## Best Practices

### Security

1. **Never commit `.env` files** with sensitive data
2. Use **environment variables** for production secrets
3. **Rotate API keys** regularly
4. **Limit CORS origins** in production

### Performance

1. **Disable debug logging** in production
2. **Enable caching** for better performance
3. **Tune worker counts** based on CPU cores
4. **Monitor resource usage** and adjust limits

### Development

1. Use **separate `.env` files** for each environment
2. **Document all custom settings** in your README
3. **Version control** example configurations
4. **Test configuration changes** before deploying

## Troubleshooting Configuration

### Common Issues

**Issue**: Settings not taking effect
```bash
# Check loaded configuration
python -c "from mcp_server.config import get_settings; print(get_settings().dict())"
```

**Issue**: Invalid configuration values
```bash
# Validate configuration
python -m mcp_server.config --validate
```

**Issue**: Environment variables not loading
```bash
# Check environment
printenv | grep MCP_
```

### Debug Configuration Loading

Enable configuration debug logging:

```python
import logging
logging.basicConfig(level=logging.DEBUG)

from mcp_server.config import get_settings
settings = get_settings()  # Will log configuration sources
```

## Next Steps

- Learn about [API Reference](./api-reference) for endpoint details
- Explore [Examples](./examples) for different configurations
- Read [Troubleshooting](./troubleshooting) for common issues