---
sidebar_position: 4
---

# Project Schema

The Project Schema (`project-schema.json`) defines the overall structure of a Brobot Runner project configuration. This schema represents the "what" of your automation - the states, transitions, and UI elements that make up your automation environment.

## Root Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| id | integer | Yes | Unique identifier for the project |
| name | string | Yes | Name of the project |
| description | string | No | Optional description of the project |
| version | string | No | Version of the project configuration (format: x.y.z) |
| created | string (date-time) | No | When the project was created |
| updated | string (date-time) | No | When the project was last updated |
| states | array | Yes | States defined in the project |
| stateTransitions | array | Yes | Transitions between states |
| automation | object | No | Automation configuration |
| configuration | object | No | Project-wide configuration settings |

## States

States represent distinct screens or conditions in your application. Each state contains visual elements that can be detected during automation.

Example state definition:

```json
{
  "id": 1,
  "name": "LoginScreen",
  "stateText": ["Welcome", "Login"],
  "blocking": true,
  "canHide": [2, 3],
  "pathScore": 1,
  "baseProbabilityExists": 100,
  "stateImages": [
    {
      "id": 1,
      "name": "LoginButton",
      "patterns": [
        {
          "name": "blueLoginBtn",
          "imgPath": "images/login_blue.png",
          "fixed": true
        }
      ]
    }
  ]
}
```

State Properties

| Property | Type | Description |
|----------|------|-------------|
| id | integer | Unique identifier for the state |
| name | string | Name of the state |
| stateText | array of strings | Text associated with this state |
| blocking | boolean | If true, this state needs to be acted on before accessing other states |
| canHide | array of integers | States that this state can hide when it becomes active |
| pathScore | integer | Larger path scores discourage taking a path with this state |
| baseProbabilityExists | integer | Base probability that the state exists (0-100) |
| usableArea | object | Region defining the usable area of the state |

GUI Elements  
The project schema supports various GUI elements that can be associated with states:  
State Images  
Images that can be detected in a state:  
```json
{
  "id": 5,
  "name": "SubmitButton",
  "shared": false,
  "patterns": [
    {
      "name": "blueSubmit",
      "imgPath": "images/submit_blue.png",
      "fixed": true,
      "searchRegions": [
        {"x": 100, "y": 200, "w": 300, "h": 100}
      ]
    }
  ]
}
```

Pattern Properties

| Property | Type | Description |
|----------|------|-------------|
| name | string | Name of the pattern |
| imgPath | string | Path to the image file |
| fixed | boolean | If true, this image should always appear in the same location |
| dynamic | boolean | If true, this pattern cannot be found using pattern matching |
| searchRegions | array | Regions to search for this pattern |
| fixedRegion | object | Fixed region where this pattern appears |
| targetPosition | object | Target position within the pattern |
| targetOffset | object | Offset for click or drag operations |

State Regions  
Regions define rectangular areas within the GUI:  
```json
{
  "id": 3,
  "name": "UsernameField",
  "searchRegion": {
    "x": 100,
    "y": 200,
    "w": 200,
    "h": 30
  }
}
```

State Locations  
Locations define specific points:  
```json
{
  "id": 2,
  "name": "CloseButton",
  "location": {
    "x": 800,
    "y": 50
  }
}
```

State Strings  
Strings define text that can be entered or detected:  
```json
{
  "id": 1,
  "name": "Username",
  "string": "admin"
}
```

State Transitions  
State transitions define how to move from one state to another using actions.  
```json
{
  "id": 1,
  "sourceStateId": 1,
  "stateImageId": 1,
  "actionDefinition": {
    "steps": [
      {
        "actionOptions": {
          "action": "CLICK",
          "clickType": "LEFT"
        },
        "objectCollection": {
          "stateImages": [1]
        }
      }
    ]
  },
  "statesToEnter": [2],
  "statesToExit": [1],
  "staysVisibleAfterTransition": "FALSE"
}
```

Transition Properties

| Property | Type | Description |
|----------|------|-------------|
| id | integer | Unique identifier for the transition |
| sourceStateId | integer | The state from which the transition starts |
| stateImageId | integer | Optional state image associated with the transition |
| actionDefinition | object | Definition of actions to perform during the transition |
| staysVisibleAfterTransition | string | Whether the source state stays visible after the transition |
| statesToEnter | array | States to enter after the transition |
| statesToExit | array | States to exit after the transition |
| score | integer | Score for path planning |

Action Definitions and Steps  
Action definitions contain sequences of steps that perform GUI operations. Each step combines action options with objects to act upon.  
```json
{
  "steps": [
    {
      "actionOptions": {
        "action": "FIND",
        "find": "FIRST",
        "similarity": 0.8,
        "maxWait": 5
      },
      "objectCollection": {
        "stateImages": [5, 6]
      }
    },
    {
      "actionOptions": {
        "action": "CLICK"
      },
      "objectCollection": {
        "stateImages": [5]
      }
    }
  ]
}
```

Action Options  
ActionOptions configure how actions are performed. As noted in the design notes, complex actions like DRAG may utilize multiple ActionOptions variables.  
Common Action Options

| Option | Type | Description |
|--------|------|-------------|
| action | enum | The action to perform (FIND, CLICK, DRAG, etc.) |
| find | enum | How to find objects (FIRST, BEST, ALL, etc.) |
| similarity | number | Match similarity threshold (0.0-1.0) |
| maxWait | number | Maximum time to wait in seconds |
| clickType | enum | Type of click (LEFT, RIGHT, DOUBLE_LEFT, etc.) |

Configuration
Project-wide configuration settings:
```json
{
  "minSimilarity": 0.7,
  "moveMouseDelay": 0.5,
  "delayBeforeMouseDown": 0.3,
  "imageDirectory": "images",
  "logLevel": "INFO",
  "illustrationEnabled": true
}
```

