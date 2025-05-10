---
sidebar_position: 5
---

# Automation DSL Schema

The Automation DSL (Domain-Specific Language) Schema defines a programming language for creating automation logic in Brobot Runner. This schema represents the "how" of your automation - the functions, statements, and expressions that control behavior.

## Automation Functions

Automation functions are the main building blocks of the DSL. They contain sequences of statements that perform actions:

```json
{
  "id": 1,
  "name": "performLogin",
  "returnType": "boolean",
  "parameters": [
    {
      "name": "username",
      "type": "string"
    },
    {
      "name": "password",
      "type": "string"
    }
  ],
  "statements": [
    {
      "statementType": "variableDeclaration",
      "name": "options",
      "type": "actionOptions",
      "value": {
        "expressionType": "builder",
        "builderType": "actionOptions",
        "methods": [
          {
            "method": "setAction",
            "arguments": [
              {
                "expressionType": "literal",
                "valueType": "string",
                "value": "CLICK"
              }
            ]
          }
        ]
      }
    },
    {
      "statementType": "methodCall",
      "object": "action",
      "method": "perform",
      "arguments": [
        {
          "expressionType": "variable",
          "name": "options"
        },
        {
          "expressionType": "variable",
          "name": "loginButton"
        }
      ]
    }
  ]
}
```

Function Properties

| Property | Type | Description |
|----------|------|-------------|
| id | integer | Unique identifier for the function |
| name | string | Name of the function |
| description | string | Description of what the function does |
| returnType | string | Return type of the function |
| parameters | array | Parameters accepted by the function |
| statements | array | Statements that make up the function body |

Return Types

The DSL supports the following return types:

- **void** - No return value
- **boolean** - True or false value
- **string** - Text value
- **int** - Integer value
- **double** - Floating-point value
- **region** - GUI region
- **matches** - Results of a find operation
- **stateImage** - Reference to a state image
- **stateRegion** - Reference to a state region
- **object** - Generic object

Statements  

Variable Declaration  
Defines a new variable:
```json
{
  "statementType": "variableDeclaration",
  "name": "maxAttempts",
  "type": "int",
  "value": {
    "expressionType": "literal",
    "valueType": "integer",
    "value": 5
  }
}
```

Assignment  
Assigns a value to an existing variable:
```json
{
  "statementType": "assignment",
  "variable": "maxAttempts",
  "value": {
    "expressionType": "literal",
    "valueType": "integer",
    "value": 10
  }
}
```

If Statement  
Conditional execution:
```json
{
  "statementType": "if",
  "condition": {
    "expressionType": "binaryOperation",
    "operator": ">",
    "left": {
      "expressionType": "variable",
      "name": "attempts"
    },
    "right": {
      "expressionType": "literal",
      "valueType": "integer",
      "value": 3
    }
  },
  "thenStatements": [
    {
      "statementType": "return",
      "value": {
        "expressionType": "literal",
        "valueType": "boolean",
        "value": false
      }
    }
  ],
  "elseStatements": [
    {
      "statementType": "assignment",
      "variable": "attempts",
      "value": {
        "expressionType": "binaryOperation",
        "operator": "+",
        "left": {
          "expressionType": "variable",
          "name": "attempts"
        },
        "right": {
          "expressionType": "literal",
          "valueType": "integer",
          "value": 1
        }
      }
    }
  ]
}
```

ForEach Loop  
Iterates through a collection:
```json
{
  "statementType": "forEach",
  "variable": "image",
  "variableType": "stateImage",
  "collection": {
    "expressionType": "variable",
    "name": "images"
  },
  "statements": [
    {
      "statementType": "methodCall",
      "object": "action",
      "method": "perform",
      "arguments": [
        {
          "expressionType": "variable",
          "name": "options"
        },
        {
          "expressionType": "variable",
          "name": "image"
        }
      ]
    }
  ]
}
```

Return Statement  
Returns a value from a function:
```json
{
  "statementType": "return",
  "value": {
    "expressionType": "literal",
    "valueType": "boolean",
    "value": true
  }
}
```

Method Call  
Calls a method:
```json
{
  "statementType": "methodCall",
  "object": "action",
  "method": "perform",
  "arguments": [
    {
      "expressionType": "variable",
      "name": "options"
    },
    {
      "expressionType": "variable",
      "name": "loginButton"
    }
  ]
}
```

Expressions  
Expressions represent values:

Literal  
Represents a constant value:
```json
{
  "expressionType": "literal",
  "valueType": "string",
  "value": "Hello World"
}
```

Variable  
References a variable:
```json
{
  "expressionType": "variable",
  "name": "loginButton"
}
```

Method Call Expression  
Calls a method and returns its result:
```json
{
  "expressionType": "methodCall",
  "object": "action",
  "method": "find",
  "arguments": [
    {
      "expressionType": "variable",
      "name": "options"
    }
  ]
}
```

Binary Operation  
Performs an operation on two expressions:
```json
{
  "expressionType": "binaryOperation",
  "operator": "+",
  "left": {
    "expressionType": "variable",
    "name": "count"
  },
  "right": {
    "expressionType": "literal",
    "valueType": "integer",
    "value": 1
  }
}
```

Builder Pattern  
The DSL extensively uses the builder pattern for creating complex objects like ActionOptions and ObjectCollection:
```json
{
  "expressionType": "builder",
  "builderType": "actionOptions",
  "methods": [
    {
      "method": "setAction",
      "arguments": [
        {
          "expressionType": "literal",
          "valueType": "string",
          "value": "FIND"
        }
      ]
    },
    {
      "method": "setMaxWait",
      "arguments": [
        {
          "expressionType": "literal",
          "valueType": "double",
          "value": 5.0
        }
      ]
    }
  ]
}
```

Custom Objects  

ActionOptions  
Configures how actions are performed:
```json
{
  "expressionType": "builder",
  "builderType": "actionOptions",
  "methods": [
    {
      "method": "setAction",
      "arguments": [
        {
          "expressionType": "literal",
          "valueType": "string",
          "value": "CLICK"
        }
      ]
    },
    {
      "method": "setClickType",
      "arguments": [
        {
          "expressionType": "literal",
          "valueType": "string",
          "value": "LEFT"
        }
      ]
    }
  ]
}
```

ObjectCollection  
Groups objects for actions:
```json
{
  "expressionType": "builder",
  "builderType": "objectCollection",
  "methods": [
    {
      "method": "withImages",
      "arguments": [
        {
          "expressionType": "variable",
          "name": "loginButton"
        }
      ]
    }
  ]
}
```

Example Use Cases  

Simple Login Automation
```json
{
  "name": "performLogin",
  "returnType": "boolean",
  "statements": [
    {
      "statementType": "variableDeclaration",
      "name": "findOptions",
      "type": "actionOptions",
      "value": {
        "expressionType": "builder",
        "builderType": "actionOptions",
        "methods": [
          {
            "method": "setAction",
            "arguments": [
              {
                "expressionType": "literal",
                "valueType": "string",
                "value": "FIND"
              }
            ]
          },
          {
            "method": "setMaxWait",
            "arguments": [
              {
                "expressionType": "literal",
                "valueType": "double",
                "value": 5.0
              }
            ]
          }
        ]
      }
    },
    {
      "statementType": "variableDeclaration",
      "name": "findResult",
      "type": "matches",
      "value": {
        "expressionType": "methodCall",
        "object": "action",
        "method": "perform",
        "arguments": [
          {
            "expressionType": "variable",
            "name": "findOptions"
          },
          {
            "expressionType": "variable",
            "name": "loginButton"
          }
        ]
      }
    },
    {
      "statementType": "if",
      "condition": {
        "expressionType": "methodCall",
        "object": "findResult",
        "method": "isSuccess",
        "arguments": []
      },
      "thenStatements": [
        {
          "statementType": "methodCall",
          "object": "action",
          "method": "perform",
          "arguments": [
            {
              "expressionType": "builder",
              "builderType": "actionOptions",
              "methods": [
                {
                  "method": "setAction",
                  "arguments": [
                    {
                      "expressionType": "literal",
                      "valueType": "string",
                      "value": "CLICK"
                    }
                  ]
                }
              ]
            },
            {
              "expressionType": "variable",
              "name": "loginButton"
            }
          ]
        },
        {
          "statementType": "return",
          "value": {
            "expressionType": "literal",
            "valueType": "boolean",
            "value": true
          }
        }
      ],
      "elseStatements": [
        {
          "statementType": "return",
          "value": {
            "expressionType": "literal",
            "valueType": "boolean",
            "value": false
          }
        }
      ]
    }
  ]
}
```
