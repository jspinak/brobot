# DSL JSON Integration Guide

## Overview

The Brobot DSL (Domain Specific Language) supports both JSON-based configuration and programmatic creation through the fluent API. Both approaches produce the same underlying data structures, ensuring full interoperability.

## Key DSL Classes

### 1. InstructionSet
- **Purpose**: Root container for automation functions
- **JSON field**: `automationFunctions` (array of BusinessTask)
- **Fluent API**: Created by `Brobot.buildSequence().build()`

### 2. BusinessTask
- **Purpose**: Represents a reusable automation function
- **JSON fields**:
  - `id`: Unique identifier
  - `name`: Function name
  - `description`: Human-readable description
  - `returnType`: Return type (void, boolean, string, taskSequence, etc.)
  - `parameters`: Function parameters
  - `statements`: Function body statements

### 3. TaskSequence
- **Purpose**: Ordered list of automation steps
- **JSON field**: `steps` (array of ActionStep)
- **Fluent API**: Built internally by ActionSequenceBuilder

### 4. ActionStep
- **Purpose**: Single automation action
- **JSON fields**:
  - `actionOptions`: Configuration for the action
  - `objectCollection`: Target objects (StateImage, StateString, etc.)

## Supported Types

The schema has been updated to support all DSL types:
- Basic types: `boolean`, `string`, `int`, `double`
- Brobot types: `region`, `matches`, `stateImage`, `stateRegion`, `stateString`
- DSL types: `actionOptions`, `objectCollection`, `taskSequence`
- Generic: `object`

## JSON Example

```json
{
  "automationFunctions": [
    {
      "name": "login",
      "description": "Automated login sequence",
      "returnType": "void",
      "parameters": [
        {
          "name": "username",
          "type": "stateString"
        },
        {
          "name": "password",
          "type": "stateString"
        }
      ],
      "statements": [
        {
          "statementType": "variableDeclaration",
          "name": "loginSequence",
          "type": "taskSequence",
          "value": {
            "expressionType": "builder",
            "builderType": "taskSequence",
            "methods": [
              {
                "method": "addStep",
                "arguments": [
                  {
                    "expressionType": "literal",
                    "valueType": "object",
                    "value": {
                      "actionOptions": {
                        "action": "FIND"
                      },
                      "objectCollection": {
                        "stateImages": [{"name": "userField"}]
                      }
                    }
                  }
                ]
              }
            ]
          }
        }
      ]
    }
  ]
}
```

## Fluent API Equivalent

```java
InstructionSet instructionSet = Brobot.buildSequence()
    .withName("login")
    .withDescription("Automated login sequence")
    .find(userField)
    .thenClick()
    .thenType(username)
    .find(passwordField)
    .thenClick()
    .thenType(password)
    .build();
```

## Serialization

The DSL classes use standard Jackson annotations:
- `@Data` - Lombok generates getters/setters
- `@JsonIgnoreProperties(ignoreUnknown = true)` - Forward compatibility

No custom serializers are needed for DSL classes as they follow standard Java bean conventions.

## Schema Validation

The `automation-dsl-schema.json` validates:
1. Structure of automation functions
2. Valid statement types
3. Expression formats
4. Type safety for variables and parameters

## Integration Points

1. **BrobotObjectMapper**: Configured with mixins and serializers for Brobot domain objects
2. **ConfigurationParser**: Handles JSON parsing with fallback for problematic objects
3. **AutomationDSLValidator**: Validates DSL JSON against schema
4. **ActionSequenceBuilder**: Creates DSL-compatible structures programmatically

## Best Practices

1. **Type Consistency**: Always use proper state objects (StateImage, StateString) rather than raw types
2. **Schema Compliance**: Validate JSON against the schema before execution
3. **Interoperability**: Structures created by fluent API can be serialized to JSON and vice versa
4. **Error Handling**: The schema validator provides detailed error messages for malformed DSL