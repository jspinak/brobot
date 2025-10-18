---
sidebar_position: 4
---

# API Reference

:::warning EXPERIMENTAL API

**This API is experimental and under active development.** While core endpoints are implemented and functional, some advanced features and CLI integration are still in development.

**Current Status**:
- ✅ Core endpoints functional (health, state_structure, observation, execute)
- ✅ Mock mode fully operational for testing
- ⚠️ CLI integration requires Brobot CLI JAR (experimental)
- ⚠️ Some documented parameters not yet implemented
- ⚠️ API may change in future versions

**Use for research, prototyping, and development purposes.**

:::

Complete reference documentation for all MCP Server API endpoints.

## Base URL

```
http://localhost:8000/api/v1
```

## Authentication

Currently, the API does not require authentication. Future versions will support API key authentication.

## Content Types

- **Request**: `application/json`
- **Response**: `application/json`
- **Encoding**: `UTF-8`

## Response Format

All responses use JSON format with UTF-8 encoding:

```http
Content-Type: application/json
```

:::info FUTURE FEATURE
Request tracking headers (X-Request-ID, X-Response-Time) are planned for future releases.
:::

## Endpoints

### Health Check

#### `GET /api/v1/health`

Check server health and Brobot CLI connectivity.

**Response**

```json
{
  "status": "ok",
  "version": "0.1.0",
  "brobot_connected": true,
  "timestamp": "2024-01-20T10:30:00Z"
}
```

**Status Codes**
- `200 OK`: Server is healthy
- `503 Service Unavailable`: Server is unhealthy

**Example**

```bash
curl http://localhost:8000/api/v1/health
```

---

### Get State Structure

#### `GET /api/v1/state_structure`

Retrieve the complete state structure of the target application.

**Response**

```json
{
  "states": [
    {
      "name": "main_menu",
      "description": "Application main menu",
      "images": [
        "main_menu_logo.png",
        "menu_button.png"
      ],
      "transitions": [
        {
          "from_state": "main_menu",
          "to_state": "login_screen",
          "action": "click_login",
          "probability": 0.95
        }
      ],
      "is_initial": true,
      "is_final": false
    }
  ],
  "current_state": "main_menu",
  "metadata": {
    "application": "MyApp",
    "version": "1.0.0",
    "last_updated": "2024-01-20T10:30:00Z"
  }
}
```

**Response Schema**

| Field | Type | Description |
|-------|------|-------------|
| `states` | array | List of all application states |
| `states[].name` | string | Unique state identifier |
| `states[].description` | string | Human-readable description |
| `states[].images` | array | Associated image patterns |
| `states[].transitions` | array | Possible state transitions |
| `states[].is_initial` | boolean | Whether this is a starting state |
| `states[].is_final` | boolean | Whether this is an ending state |
| `current_state` | string | Currently active state name |
| `metadata` | object | Additional information |

**Status Codes**
- `200 OK`: Success
- `500 Internal Server Error`: CLI error

**Example**

```python
import requests

response = requests.get("http://localhost:8000/api/v1/state_structure")
states = response.json()["states"]
print(f"Found {len(states)} states")
```

---

### Get Observation

#### `GET /api/v1/observation`

Get current observation including screenshot and active states.

:::info NOTE
Currently, this endpoint always returns a full observation with screenshot. Query parameters for filtering are planned for future releases.
:::

**Response**

```json
{
  "timestamp": "2024-01-20T10:30:00Z",
  "active_states": [
    {
      "name": "dashboard",
      "confidence": 0.95,
      "matched_patterns": [
        "dashboard_header.png",
        "user_menu.png"
      ]
    },
    {
      "name": "notifications",
      "confidence": 0.72,
      "matched_patterns": [
        "notification_bell.png"
      ]
    }
  ],
  "screenshot": "iVBORw0KGgoAAAANS...",
  "screen_width": 1920,
  "screen_height": 1080,
  "metadata": {
    "capture_duration": 0.125,
    "analysis_duration": 0.087,
    "total_patterns_checked": 42,
    "patterns_matched": 3
  }
}
```

**Response Schema**

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | string | ISO 8601 timestamp |
| `active_states` | array | Currently active states |
| `active_states[].name` | string | State name |
| `active_states[].confidence` | float | Confidence score (0.0-1.0) |
| `active_states[].matched_patterns` | array | Patterns that matched |
| `screenshot` | string | Base64 encoded image |
| `screen_width` | integer | Screen width in pixels |
| `screen_height` | integer | Screen height in pixels |
| `metadata` | object | Performance metrics |

**Status Codes**
- `200 OK`: Success
- `500 Internal Server Error`: Screenshot capture failed

**Example**

```python
import requests
import base64

# Get observation (always includes screenshot)
response = requests.get("http://localhost:8000/api/v1/observation")
obs = response.json()

# Save screenshot to file
if obs.get("screenshot"):
    img_data = base64.b64decode(obs["screenshot"])
    with open("screen.png", "wb") as f:
        f.write(img_data)
```

---

### Execute Action

#### `POST /api/v1/execute`

Execute an automation action on the target application.

**Request Body**

```json
{
  "action_type": "click",
  "parameters": {
    "image_pattern": "submit_button.png",
    "confidence": 0.9,
    "timeout": 5.0
  },
  "target_state": "form_submitted",
  "timeout": 10.0
}
```

**Request Schema**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `action_type` | string | Yes | Type of action to execute |
| `parameters` | object | Yes | Action-specific parameters |
| `target_state` | string | No | Expected state after action |
| `timeout` | float | No | Action timeout in seconds (default: 10.0) |

**Response**

```json
{
  "success": true,
  "action_type": "click",
  "duration": 0.523,
  "result_state": "form_submitted",
  "error": null,
  "metadata": {
    "click_location": {
      "x": 640,
      "y": 480
    },
    "pattern_found": true,
    "confidence": 0.92,
    "search_time": 0.234
  }
}
```

**Response Schema**

| Field | Type | Description |
|-------|------|-------------|
| `success` | boolean | Whether action succeeded |
| `action_type` | string | Type of action executed |
| `duration` | float | Execution time in seconds |
| `result_state` | string | State after execution |
| `error` | string | Error message if failed |
| `metadata` | object | Action-specific details |

**Status Codes**
- `200 OK`: Action completed (check `success` field)
- `400 Bad Request`: Invalid parameters
- `422 Unprocessable Entity`: Validation error
- `500 Internal Server Error`: Execution error

---

## Action Types

:::tip PARAMETER SIMPLIFICATION
The API provides simplified parameters for common use cases. Advanced Brobot configuration options (search regions, verification options, repetition options) can be accessed through the Brobot CLI directly.
:::

### Click Action

Click on an image pattern or specific location.

**Parameters**

```json
{
  "action_type": "click",
  "parameters": {
    "image_pattern": "button.png",  // OR use location
    "location": {"x": 500, "y": 300},
    "confidence": 0.9,
    "click_type": "single",  // single|double|right
    "offset": {"x": 0, "y": 0}
  }
}
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `image_pattern` | string | No* | Image file to find |
| `location` | object | No* | Click coordinates |
| `confidence` | float | No | Min similarity (0.0-1.0) |
| `click_type` | string | No | Click type (default: single) |
| `offset` | object | No | Offset from pattern center |

*Either `image_pattern` or `location` is required

### Type Action

Type text at the current cursor location.

**Parameters**

```json
{
  "action_type": "type",
  "parameters": {
    "text": "Hello, World!",
    "typing_speed": 300,  // chars per minute
    "clear_first": false,
    "press_enter": true
  }
}
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `text` | string | Yes | Text to type |
| `typing_speed` | integer | No | Typing speed (default: 300) |
| `clear_first` | boolean | No | Clear field first |
| `press_enter` | boolean | No | Press Enter after typing |

### Drag Action

Drag from one location to another.

**Parameters**

```json
{
  "action_type": "drag",
  "parameters": {
    "start_pattern": "drag_handle.png",  // OR start_x, start_y
    "start_x": 100,
    "start_y": 100,
    "end_pattern": "drop_zone.png",  // OR end_x, end_y
    "end_x": 500,
    "end_y": 500,
    "duration": 1.0,
    "button": "left"  // left|right|middle
  }
}
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `start_pattern` | string | No* | Start position pattern |
| `start_x`, `start_y` | integer | No* | Start coordinates |
| `end_pattern` | string | No* | End position pattern |
| `end_x`, `end_y` | integer | No* | End coordinates |
| `duration` | float | No | Drag duration (default: 1.0) |
| `button` | string | No | Mouse button (default: left) |

*Either pattern or coordinates required for start/end

### Find Action

Find an image pattern without performing any action.

**Parameters**

```json
{
  "action_type": "find",
  "parameters": {
    "image_pattern": "element.png",
    "confidence": 0.9,
    "timeout": 5.0
  }
}
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `image_pattern` | string | Yes | Image file to find |
| `confidence` | float | No | Min similarity (0.0-1.0) |
| `timeout` | float | No | Search timeout (default: 5.0) |

:::info ADDITIONAL ACTION TYPES
The Brobot framework supports additional action types including `vanish` (wait for element to disappear), `hover`, `double_click`, `right_click`, and more. See the [Brobot action documentation](../../01-getting-started/action-hierarchy.md) for the complete list.
:::

## Error Handling

### Error Response Format

The API uses FastAPI's standard error response format:

**Simple Errors (4xx, 5xx)**:
```json
{
  "detail": "Error message"
}
```

**Validation Errors (422)**:
```json
{
  "detail": [
    {
      "loc": ["body", "field_name"],
      "msg": "error description",
      "type": "error_type"
    }
  ]
}
```

### Common Error Codes

| Status Code | Description | Usage |
|-------------|-------------|-------|
| `422` | Validation error | Invalid request parameters |
| `500` | Internal server error | CLI errors, execution failures |

:::info ERROR HANDLING
The API currently uses simplified error responses. Future versions may include additional error context and custom error types.
:::

### Error Examples

**Validation Error (422)**
```json
{
  "detail": [
    {
      "loc": ["body", "action_type"],
      "msg": "field required",
      "type": "value_error.missing"
    }
  ]
}
```

**CLI Error (500)**
```json
{
  "detail": "Brobot CLI error: Pattern not found",
  "type": "cli_error",
  "ctx": {
    "pattern": "nonexistent.png",
    "search_time": 5.0
  }
}
```

## Rate Limiting

Future versions will implement rate limiting:

```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 1642521600
```

## Pagination

Future versions will support pagination for large responses:

```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "per_page": 20,
    "total": 100,
    "pages": 5
  }
}
```

## Webhooks

Future versions will support webhooks for state changes:

```json
{
  "event": "state_changed",
  "timestamp": "2024-01-20T10:30:00Z",
  "data": {
    "from_state": "login",
    "to_state": "dashboard",
    "trigger": "user_action"
  }
}
```

## Client Libraries

### Python

```python
from brobot_client import BrobotClient

client = BrobotClient("http://localhost:8000")
result = client.click("button.png")
```

### JavaScript (Coming Soon)

```javascript
const client = new BrobotClient("http://localhost:8000");
await client.click("button.png");
```

### cURL Examples

```bash
# Get observation
curl http://localhost:8000/api/v1/observation

# Execute click
curl -X POST http://localhost:8000/api/v1/execute \
  -H "Content-Type: application/json" \
  -d '{"action_type":"click","parameters":{"image_pattern":"button.png"}}'

# Pretty print with jq
curl http://localhost:8000/api/v1/state_structure | jq .
```

## API Versioning

The API uses URL versioning. Current version: `v1`

Future versions will maintain backwards compatibility or provide migration guides.

## OpenAPI Specification

Access the OpenAPI (Swagger) specification:

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc
- **OpenAPI JSON**: http://localhost:8000/openapi.json

## Next Steps

After reviewing the API reference:

- 📦 Complete [Installation](./installation.md) if you haven't already
- 🚀 Follow the [Getting Started](./getting-started.md) tutorial
- ⚙️ Review [Configuration](./configuration.md) options
- 💡 Explore [Examples](./examples.md) for practical use cases
- 🐛 Check [Troubleshooting](./troubleshooting.md) if you encounter issues

## Support

:::info COMMUNITY SUPPORT ONLY
Support for this experimental API is limited to community contributions. There are no guarantees of responses or fixes.
:::

- **GitHub Issues**: [Report API issues](https://github.com/jspinak/brobot-mcp-server/issues)
- **Documentation**: Full docs at [brobot.dev](https://brobot.dev)