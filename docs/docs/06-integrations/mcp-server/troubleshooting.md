---
sidebar_position: 6
---

# Troubleshooting Guide

:::caution EXPERIMENTAL TROUBLESHOOTING - LIMITED COVERAGE

**This troubleshooting guide covers theoretical issues for an untested experimental feature.** Many real-world problems may not be documented here, and suggested solutions may not work.

**Key Warnings**:
- Issues described may not match actual problems encountered
- Solutions provided are untested and may not resolve issues
- New error types not covered in this guide are likely
- Debug techniques may require modifications to work
- Community support is limited for troubleshooting

**Expect to encounter undocumented issues when using this experimental software.**

:::

Common issues and solutions when using the Brobot MCP Server.

## Server Issues

### Server Won't Start

#### Symptom
```bash
$ python -m mcp_server.main
ModuleNotFoundError: No module named 'mcp_server'
```

#### Solutions

1. **Check Python version**
   ```bash
   python --version  # Should be 3.8 or higher
   ```

2. **Install in development mode**
   ```bash
   pip install -e .
   ```

3. **Verify installation**
   ```bash
   pip list | grep brobot
   ```

4. **Check virtual environment**
   ```bash
   # Ensure you're in the correct venv
   which python
   # Should show your venv path
   ```

---

### Port Already in Use

#### Symptom
```
ERROR:    [Errno 48] Address already in use
```

#### Solutions

1. **Find process using port**
   ```bash
   # Linux/Mac
   lsof -i :8000
   
   # Windows
   netstat -ano | findstr :8000
   ```

2. **Kill the process**
   ```bash
   # Linux/Mac
   kill -9 <PID>
   
   # Windows
   taskkill /PID <PID> /F
   ```

3. **Use different port**
   ```bash
   MCP_PORT=8080 python -m mcp_server.main
   ```

---

### Import Errors

#### Symptom
```
ImportError: cannot import name 'BaseSettings' from 'pydantic'
```

#### Solution
Install correct Pydantic version:
```bash
pip install pydantic-settings
```

## CLI Integration Issues

### CLI JAR Not Found

#### Symptom
```
FileNotFoundError: Brobot CLI JAR not found at: brobot-cli.jar
```

#### Solutions

1. **Build the CLI**
   ```bash
   cd brobot-cli
   gradle shadowJar
   # or
   ./gradlew shadowJar
   ```

2. **Check JAR location**
   ```bash
   ls brobot-cli/build/libs/
   ```

3. **Update configuration**
   ```env
   BROBOT_CLI_JAR=/absolute/path/to/brobot-cli.jar
   ```

---

### Java Not Found

#### Symptom
```
subprocess.CalledProcessError: Command '['java', '-jar', ...]' returned non-zero exit status 127
```

#### Solutions

1. **Install Java**
   ```bash
   # Ubuntu/Debian
   sudo apt install openjdk-11-jdk
   
   # macOS
   brew install openjdk@11
   
   # Windows
   winget install Microsoft.OpenJDK.11
   ```

2. **Check Java installation**
   ```bash
   java -version
   javac -version
   ```

3. **Set JAVA_HOME**
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home)  # macOS
   export JAVA_HOME=/usr/lib/jvm/java-11-openjdk  # Linux
   ```

---

### CLI Timeout Errors

#### Symptom
```
BrobotCLIError: Command timed out after 30 seconds
```

#### Solutions

1. **Increase timeout**
   ```env
   CLI_TIMEOUT=60.0
   ```

2. **Check system performance**
   ```bash
   # Monitor CPU/memory during execution
   top  # or htop
   ```

3. **Reduce CLI load**
   ```bash
   # Check if other Java processes are consuming resources
   ps aux | grep java

   # Use mock mode to isolate if issue is CLI or server
   USE_MOCK_DATA=true python -m mcp_server.main
   ```

## API Issues

### 500 Internal Server Error

#### Symptom
```json
{
  "detail": "Internal server error"
}
```

#### Solutions

1. **Check server logs**
   ```bash
   # Enable debug logging
   MCP_LOG_LEVEL=debug python -m mcp_server.main
   ```

2. **Test in mock mode**
   ```env
   USE_MOCK_DATA=true
   ```

3. **Validate CLI directly**
   ```bash
   java -jar brobot-cli.jar get-state-structure
   ```

---

### Pattern Not Found

#### Symptom
```json
{
  "success": false,
  "error": "Pattern not found: button.png"
}
```

#### Solutions

1. **Verify pattern exists**
   ```bash
   ls patterns/  # Check your pattern directory
   ```

2. **Lower confidence threshold**
   ```python
   from brobot_client import BrobotClient

   client = BrobotClient()
   client.click("button.png", confidence=0.7)
   ```

3. **Save screenshot for debugging**
   ```python
   from brobot_client import BrobotClient

   client = BrobotClient()
   obs = client.get_observation()

   if obs.save_screenshot("debug.png"):
       print("Screenshot saved successfully")
       # Review debug.png to see what patterns are visible
   ```

---

### State Not Detected

#### Symptom
```json
{
  "active_states": [],
  "screenshot": "..."
}
```

#### Solutions

1. **Check state configuration**
   - Verify state images exist
   - Ensure patterns are up-to-date
   - Check that images directory is accessible to the CLI

2. **Test in mock mode first**
   ```env
   # Verify API works correctly
   USE_MOCK_DATA=true
   ```

   If mock mode shows states but real mode doesn't, the issue is with Brobot CLI configuration.

3. **Debug state detection**
   ```python
   from brobot_client import BrobotClient

   client = BrobotClient()

   # Get detailed state info
   structure = client.get_state_structure()
   for state in structure.states:
       print(f"{state.name}: {state.images}")
   ```

## Client Issues

### Connection Refused

#### Symptom
```python
BrobotConnectionError: Failed to connect to server at http://localhost:8000
```

#### Solutions

1. **Verify server is running**
   ```bash
   curl http://localhost:8000/health
   ```

2. **Check firewall**
   ```bash
   # Linux
   sudo ufw status
   
   # Windows
   netsh advfirewall show allprofiles
   ```

3. **Use correct URL**
   ```python
   from brobot_client import BrobotClient

   client = BrobotClient("http://127.0.0.1:8000")  # Try IP instead
   ```

---

### Timeout Errors

#### Symptom
```python
BrobotTimeoutError: Request timed out after 30s
```

#### Solutions

1. **Increase client timeout**
   ```python
   from brobot_client import BrobotClient

   client = BrobotClient(timeout=60.0)
   ```

2. **Check network latency**
   ```bash
   ping localhost
   ```

3. **Use longer timeouts for slow operations**
   ```python
   # Set longer timeout for specific operations
   result = client.click("slow_button.png", timeout=90.0)
   ```

## Performance Issues

### Slow Pattern Matching

#### Solutions

1. **Optimize image patterns**
   - Use smaller images
   - Remove unnecessary details
   - Use distinctive features

2. **Use lower confidence thresholds**
   ```python
   # Lower threshold for faster (but less accurate) matching
   client.click("button.png", confidence=0.7)
   ```

3. **Optimize timeout settings**
   ```env
   # Reduce CLI timeout if operations are simple
   CLI_TIMEOUT=15.0
   ```

---

### High Memory Usage

:::info FUTURE FEATURE
Advanced memory management and performance tuning options are not yet implemented in the MCP server. These features may be added in future releases.
:::

#### Current Solutions

1. **Use mock mode for testing**
   ```env
   USE_MOCK_DATA=true  # Reduces memory usage
   ```

2. **Restart server periodically**
   ```bash
   # Restart server to free memory
   # Stop current server (Ctrl+C)
   python -m mcp_server.main
   ```

3. **Monitor memory usage**
   ```bash
   # Check memory usage
   ps aux | grep -E "(java|python)" | grep -v grep
   ```

## Docker Issues

### Container Can't Access Display

#### Symptom
```
Error: Cannot open display
```

#### Solutions

1. **Linux: Share X11 socket**
   ```bash
   docker run -e DISPLAY=$DISPLAY \
     -v /tmp/.X11-unix:/tmp/.X11-unix \
     brobot-mcp-server
   ```

2. **macOS: Use XQuartz**
   ```bash
   # Install XQuartz
   brew install --cask xquartz
   
   # Allow connections
   xhost +localhost
   ```

3. **Windows: Use X server**
   - Install VcXsrv or similar
   - Configure display forwarding

## Common Error Messages

### "No module named 'cv2'"

**Solution**: Install OpenCV
```bash
pip install opencv-python
```

### "Failed to validate Brobot CLI"

**Solution**: Test CLI manually
```bash
java -jar brobot-cli.jar --version
```

### "Invalid JSON response from CLI"

**Solution**: Check CLI output format
```bash
java -jar brobot-cli.jar get-state-structure | jq .
```

## Debug Techniques

### Enable Verbose Logging

```python
import logging
logging.basicConfig(level=logging.DEBUG)

# Now client will show detailed logs
client = BrobotClient()
```

### Save Debug Information

```python
import json
from datetime import datetime
from brobot_client import BrobotClient

client = BrobotClient()

def debug_automation():
    try:
        result = client.click("button.png")
    except Exception as e:
        # Save debug info
        obs = client.get_observation()
        obs.save_screenshot("error_screenshot.png")

        with open("debug_log.json", "w") as f:
            json.dump({
                "error": str(e),
                "active_states": [s.name for s in obs.active_states],
                "timestamp": datetime.now().isoformat()
            }, f, indent=2)

        raise
```

### Monitor System Resources

```bash
# Watch resource usage during automation
watch -n 1 'ps aux | grep -E "(java|python)" | grep -v grep'
```

## Related Documentation

Before seeking help, review these related guides:

- 📦 **[Installation Guide](./installation.md)** - Platform-specific setup and system requirements
- ⚙️ **[Configuration Guide](./configuration.md)** - All configuration options explained
- 🚀 **[Getting Started](./getting-started.md)** - Basic usage patterns and examples
- 📖 **[API Reference](./api-reference.md)** - Complete endpoint documentation
- 💡 **[Examples](./examples.md)** - Integration patterns with AI services

For core Brobot framework issues (not MCP server specific):
- **[Brobot Installation](../../01-getting-started/installation.md)** - Core framework setup
- **[Brobot States](../../01-getting-started/states.md)** - Understanding state-based automation
- **[Brobot Core Concepts](../../01-getting-started/core-concepts.md)** - Framework architecture

## Getting Help

:::info COMMUNITY SUPPORT ONLY
Support for this experimental feature is limited to community contributions. Response times may be slow, and many issues may remain unresolved.
:::

If these solutions don't resolve your issue:

1. **Review related documentation** - Check the guides listed above
2. **Search existing issues**: [GitHub Issues](https://github.com/jspinak/brobot-mcp-server/issues)
3. **Create detailed bug report** with:
   - Error messages
   - System information (OS, Python/Java versions)
   - Configuration files (.env contents)
   - Steps to reproduce
   - Relevant log output
4. **Include environment details**:
   ```bash
   python --version
   java -version
   pip list | grep brobot
   ```

## FAQ

**Q: Can I run MCP server on a headless system?**
A: Yes, use a virtual display like Xvfb:
```bash
xvfb-run -a python -m mcp_server.main
```

**Q: How do I update image patterns?**
A: Place new images in your patterns directory and restart the server.

**Q: Can multiple clients connect simultaneously?**
A: Yes, the server handles multiple connections, but actions are serialized.

**Q: Is Windows support available?**
A: Yes, but some features may require additional configuration.

**Q: How do I contribute fixes?**
A: Fork the repository, make changes, and submit a pull request!