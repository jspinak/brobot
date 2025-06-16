# Brobot Configuration Format Documentation

## Overview

Brobot configurations are JSON files that define GUI automation workflows. This document provides a comprehensive reference for the configuration format, including all supported elements, properties, and validation rules.

## Table of Contents

1. [Basic Structure](#basic-structure)
2. [Configuration Properties](#configuration-properties)
3. [States](#states)
4. [Images](#images)
5. [Actions](#actions)
6. [Transitions](#transitions)
7. [Variables](#variables)
8. [Settings](#settings)
9. [Advanced Features](#advanced-features)
10. [Validation Rules](#validation-rules)
11. [Examples](#examples)

## Basic Structure

A minimal Brobot configuration has the following structure:

```json
{
  "name": "Configuration Name",
  "version": "1.0.0",
  "description": "Description of what this automation does",
  "states": []
}
```

### Required Fields
- `name` (string): Unique identifier for the configuration
- `states` (array): List of automation states

### Optional Fields
- `version` (string): Semantic version number
- `description` (string): Human-readable description
- `settings` (object): Global configuration settings
- `variables` (object): Reusable variables
- `metadata` (object): Additional information

## Configuration Properties

### Root Level Properties

```json
{
  "name": "string",
  "version": "string",
  "description": "string",
  "author": "string",
  "created": "ISO 8601 date",
  "modified": "ISO 8601 date",
  "tags": ["array", "of", "strings"],
  "states": [],
  "transitions": [],
  "variables": {},
  "settings": {},
  "metadata": {}
}
```

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | string | Yes | Configuration identifier |
| `version` | string | No | Semantic version (default: "1.0.0") |
| `description` | string | No | Detailed description |
| `author` | string | No | Creator's name |
| `created` | string | No | Creation timestamp |
| `modified` | string | No | Last modification timestamp |
| `tags` | array | No | Categorization tags |
| `states` | array | Yes | State definitions |
| `transitions` | array | No | State transitions |
| `variables` | object | No | Global variables |
| `settings` | object | No | Configuration settings |
| `metadata` | object | No | Custom metadata |

## States

States represent distinct screens or conditions in your application.

### State Definition

```json
{
  "name": "StateName",
  "description": "What this state represents",
  "images": [
    {
      "name": "image_identifier",
      "path": "relative/path/to/image.png",
      "similarity": 0.95
    }
  ],
  "regions": [
    {
      "name": "region_name",
      "x": 100,
      "y": 200,
      "width": 300,
      "height": 150
    }
  ],
  "actions": [],
  "onEnter": [],
  "onExit": [],
  "timeout": 30
}
```

### State Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | string | Yes | Unique state identifier |
| `description` | string | No | State description |
| `images` | array | No | Recognition images |
| `regions` | array | No | Screen regions |
| `actions` | array | No | Available actions |
| `onEnter` | array | No | Actions executed on entry |
| `onExit` | array | No | Actions executed on exit |
| `timeout` | number | No | State timeout in seconds |

## Images

Images are used for visual recognition and targeting.

### Image Definition

```json
{
  "name": "button_ok",
  "path": "images/buttons/ok.png",
  "similarity": 0.9,
  "searchRegion": {
    "x": 0,
    "y": 0,
    "width": 1920,
    "height": 1080
  },
  "offset": {
    "x": 10,
    "y": 5
  },
  "waitBefore": 0.5,
  "waitAfter": 1.0,
  "retryCount": 3,
  "cacheable": true
}
```

### Image Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | string | Yes | Image identifier |
| `path` | string | Yes | Relative path to image file |
| `similarity` | number | No | Match threshold (0.0-1.0, default: 0.9) |
| `searchRegion` | object | No | Limit search area |
| `offset` | object | No | Click offset from center |
| `waitBefore` | number | No | Seconds to wait before search |
| `waitAfter` | number | No | Seconds to wait after match |
| `retryCount` | number | No | Number of retry attempts |
| `cacheable` | boolean | No | Enable caching (default: true) |

## Actions

Actions define operations to perform.

### Action Types

#### Click Action
```json
{
  "type": "click",
  "target": "image_name",
  "button": "left",
  "clickType": "single",
  "modifiers": ["ctrl", "shift"]
}
```

#### Type Action
```json
{
  "type": "type",
  "text": "Hello World",
  "clearFirst": true,
  "pressEnter": true
}
```

#### Key Action
```json
{
  "type": "key",
  "keys": ["ctrl", "a"],
  "duration": 0.1
}
```

#### Wait Action
```json
{
  "type": "wait",
  "duration": 2.5,
  "condition": {
    "type": "imageAppears",
    "target": "loading_complete"
  }
}
```

#### Drag Action
```json
{
  "type": "drag",
  "from": "source_image",
  "to": "target_image",
  "duration": 1.0
}
```

#### Scroll Action
```json
{
  "type": "scroll",
  "direction": "down",
  "amount": 5,
  "target": "scrollable_area"
}
```

#### Conditional Action
```json
{
  "type": "conditional",
  "condition": {
    "type": "imageExists",
    "target": "error_dialog"
  },
  "ifTrue": [
    {
      "type": "click",
      "target": "close_button"
    }
  ],
  "ifFalse": [
    {
      "type": "wait",
      "duration": 1
    }
  ]
}
```

#### Loop Action
```json
{
  "type": "loop",
  "count": 5,
  "actions": [
    {
      "type": "click",
      "target": "next_button"
    },
    {
      "type": "wait",
      "duration": 0.5
    }
  ]
}
```

### Common Action Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `type` | string | Yes | Action type identifier |
| `name` | string | No | Action name for logging |
| `description` | string | No | Action description |
| `enabled` | boolean | No | Enable/disable action |
| `continueOnError` | boolean | No | Continue if action fails |
| `retryCount` | number | No | Retry attempts on failure |
| `timeout` | number | No | Action timeout in seconds |

## Transitions

Transitions define how to move between states.

### Transition Definition

```json
{
  "name": "login_to_main",
  "from": "LoginState",
  "to": "MainMenuState",
  "condition": {
    "type": "imageAppears",
    "target": "main_menu_logo",
    "timeout": 10
  },
  "actions": [
    {
      "type": "click",
      "target": "login_button"
    }
  ],
  "priority": 1
}
```

### Transition Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `name` | string | No | Transition identifier |
| `from` | string | Yes | Source state name |
| `to` | string | Yes | Target state name |
| `condition` | object | No | Trigger condition |
| `actions` | array | No | Actions to execute |
| `priority` | number | No | Execution priority |

## Variables

Variables allow dynamic values in configurations.

### Variable Definition

```json
{
  "variables": {
    "username": {
      "type": "string",
      "value": "admin",
      "description": "Login username"
    },
    "maxRetries": {
      "type": "number",
      "value": 3,
      "min": 1,
      "max": 10
    },
    "enableDebug": {
      "type": "boolean",
      "value": false
    },
    "searchRegions": {
      "type": "array",
      "value": [
        {"x": 0, "y": 0, "width": 800, "height": 600}
      ]
    }
  }
}
```

### Variable Usage

Variables can be referenced using the `${}` syntax:

```json
{
  "type": "type",
  "text": "${username}"
}
```

### Variable Types

| Type | Description | Example |
|------|-------------|---------|
| `string` | Text values | `"hello"` |
| `number` | Numeric values | `42`, `3.14` |
| `boolean` | True/false | `true`, `false` |
| `array` | List of values | `[1, 2, 3]` |
| `object` | Key-value pairs | `{"x": 10, "y": 20}` |

## Settings

Global configuration settings.

### Settings Definition

```json
{
  "settings": {
    "execution": {
      "speed": "normal",
      "delay": 0.1,
      "timeout": 30,
      "retryCount": 3,
      "continueOnError": false
    },
    "recognition": {
      "defaultSimilarity": 0.9,
      "searchMethod": "fast",
      "colorSpace": "rgb",
      "scale": 1.0
    },
    "logging": {
      "level": "info",
      "file": "logs/execution.log",
      "console": true,
      "screenshots": true
    },
    "performance": {
      "maxThreads": 4,
      "cacheImages": true,
      "gpuAcceleration": false
    }
  }
}
```

### Setting Categories

#### Execution Settings
- `speed`: Execution speed ("slow", "normal", "fast")
- `delay`: Delay between actions (seconds)
- `timeout`: Global timeout (seconds)
- `retryCount`: Default retry attempts
- `continueOnError`: Continue on failures

#### Recognition Settings
- `defaultSimilarity`: Default image match threshold
- `searchMethod`: Search algorithm ("fast", "accurate")
- `colorSpace`: Color matching ("rgb", "grayscale")
- `scale`: Image scaling factor

#### Logging Settings
- `level`: Log verbosity ("error", "warn", "info", "debug")
- `file`: Log file path
- `console`: Console output enabled
- `screenshots`: Capture screenshots

#### Performance Settings
- `maxThreads`: Thread pool size
- `cacheImages`: Enable image caching
- `gpuAcceleration`: Use GPU if available

## Advanced Features

### Custom Scripts

Embed custom logic using scripts:

```json
{
  "type": "script",
  "language": "javascript",
  "code": "return Math.random() > 0.5;",
  "timeout": 5
}
```

### Error Handlers

Define error handling strategies:

```json
{
  "errorHandlers": [
    {
      "errorType": "ImageNotFound",
      "actions": [
        {
          "type": "screenshot",
          "path": "errors/not_found_${timestamp}.png"
        },
        {
          "type": "log",
          "message": "Image not found, retrying..."
        }
      ]
    }
  ]
}
```

### Modules

Include reusable configuration modules:

```json
{
  "modules": [
    {
      "name": "CommonActions",
      "path": "modules/common.json"
    }
  ]
}
```

## Validation Rules

### Required Elements
1. Configuration must have a `name`
2. At least one state must be defined
3. State names must be unique
4. Image paths must be valid

### Best Practices
1. Use descriptive names for states and actions
2. Set appropriate timeouts
3. Include error handling
4. Document complex logic
5. Use variables for repeated values

### Common Validation Errors

| Error | Description | Solution |
|-------|-------------|----------|
| Missing required field | Required field not provided | Add the missing field |
| Invalid type | Wrong data type | Correct the data type |
| Duplicate identifier | Non-unique name | Use unique names |
| Invalid path | File not found | Verify file paths |
| Circular reference | States reference each other | Break the cycle |

## Examples

### Simple Click Automation

```json
{
  "name": "SimpleClickAutomation",
  "version": "1.0.0",
  "states": [
    {
      "name": "Start",
      "images": [
        {
          "name": "start_button",
          "path": "images/start.png"
        }
      ],
      "actions": [
        {
          "type": "click",
          "target": "start_button"
        }
      ]
    }
  ]
}
```

### Form Filling Automation

```json
{
  "name": "FormFiller",
  "version": "1.0.0",
  "variables": {
    "firstName": {"type": "string", "value": "John"},
    "lastName": {"type": "string", "value": "Doe"},
    "email": {"type": "string", "value": "john.doe@example.com"}
  },
  "states": [
    {
      "name": "FormPage",
      "actions": [
        {
          "type": "click",
          "target": "first_name_field"
        },
        {
          "type": "type",
          "text": "${firstName}"
        },
        {
          "type": "key",
          "keys": ["tab"]
        },
        {
          "type": "type",
          "text": "${lastName}"
        },
        {
          "type": "key",
          "keys": ["tab"]
        },
        {
          "type": "type",
          "text": "${email}"
        },
        {
          "type": "click",
          "target": "submit_button"
        }
      ]
    }
  ]
}
```

### Complex Workflow

```json
{
  "name": "ComplexWorkflow",
  "version": "2.0.0",
  "settings": {
    "execution": {
      "continueOnError": true,
      "timeout": 60
    }
  },
  "states": [
    {
      "name": "Login",
      "onEnter": [
        {"type": "log", "message": "Entering login state"}
      ],
      "actions": [
        {
          "type": "conditional",
          "condition": {
            "type": "imageExists",
            "target": "already_logged_in"
          },
          "ifTrue": [
            {
              "type": "transition",
              "to": "MainMenu"
            }
          ],
          "ifFalse": [
            {
              "type": "type",
              "text": "${credentials.username}"
            },
            {
              "type": "key",
              "keys": ["tab"]
            },
            {
              "type": "type",
              "text": "${credentials.password}"
            },
            {
              "type": "click",
              "target": "login_button"
            }
          ]
        }
      ]
    },
    {
      "name": "MainMenu",
      "actions": [
        {
          "type": "loop",
          "condition": {
            "type": "while",
            "expression": "${tasksRemaining} > 0"
          },
          "actions": [
            {
              "type": "click",
              "target": "next_task"
            },
            {
              "type": "wait",
              "duration": 1
            }
          ]
        }
      ]
    }
  ],
  "transitions": [
    {
      "from": "Login",
      "to": "MainMenu",
      "condition": {
        "type": "imageAppears",
        "target": "main_menu_logo"
      }
    }
  ]
}
```

## Schema Reference

For programmatic validation, the full JSON Schema is available at:
- [brobot-config-schema.json](schemas/brobot-config-schema.json)

Use this schema with JSON Schema validators to ensure configuration validity before execution.