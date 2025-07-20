---
sidebar_position: 6
---

# Troubleshooting Guide

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

3. **Optimize Brobot configuration**
   ```env
   BROBOT_SEARCH_TIMEOUT=10.0
   BROBOT_MIN_SIMILARITY=0.7  # Lower threshold
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
   client.click("button.png", confidence=0.7)
   ```

3. **Use larger search region**
   ```python
   # Future API feature
   client.click("button.png", region=Region(0, 0, 800, 600))
   ```

4. **Save screenshot for debugging**
   ```python
   obs = client.get_observation()
   obs.save_screenshot("debug.png")
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

2. **Adjust detection parameters**
   ```env
   BROBOT_MIN_SIMILARITY=0.75
   BROBOT_STATE_TIMEOUT=15.0
   ```

3. **Debug state detection**
   ```python
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
   client = BrobotClient(timeout=60.0)
   ```

2. **Use async client**
   ```python
   async with AsyncBrobotClient() as client:
       result = await client.click("button.png")
   ```

3. **Check network latency**
   ```bash
   ping localhost
   ```

## Performance Issues

### Slow Pattern Matching

#### Solutions

1. **Optimize image patterns**
   - Use smaller images
   - Remove unnecessary details
   - Use distinctive features

2. **Cache patterns**
   ```env
   PATTERN_CACHE_ENABLED=true
   ```

3. **Limit search area**
   ```python
   # Search specific region only
   client.click("button.png", region=Region(100, 100, 200, 200))
   ```

---

### High Memory Usage

#### Solutions

1. **Limit worker processes**
   ```env
   WORKERS=2  # Instead of 4
   ```

2. **Enable garbage collection**
   ```env
   GC_THRESHOLD=70
   ```

3. **Reduce cache size**
   ```env
   CACHE_MAX_SIZE=50MB
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

## Getting Help

If these solutions don't resolve your issue:

1. **Search existing issues**: [GitHub Issues](https://github.com/jspinak/brobot-mcp-server/issues)
2. **Create detailed bug report** with:
   - Error messages
   - System information
   - Configuration files
   - Steps to reproduce
3. **Join Discord**: Get real-time help from the community
4. **Check logs**: Always include relevant log output

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