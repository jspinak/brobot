```json
{
  "automationFunctions": [
    {
      "id": 1,
      "name": "navigateToPricingAndSignUp",
      "description": "Navigates to the Pricing page and signs up with the given email",
      "returnType": "boolean",
      "parameters": [
        {
          "name": "email",
          "type": "string"
        }
      ],
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
          "statementType": "methodCall",
          "object": "stateTransitionsManagement",
          "method": "openState",
          "arguments": [
            {
              "expressionType": "literal",
              "valueType": "integer",
              "value": 2
            }
          ]
        },
        {
          "statementType": "variableDeclaration",
          "name": "clickOptions",
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
              "name": "clickOptions"
            },
            {
              "expressionType": "literal",
              "valueType": "integer",
              "value": 201
            }
          ]
        },
        {
          "statementType": "variableDeclaration",
          "name": "typeOptions",
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
                    "value": "TYPE"
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
              "name": "typeOptions"
            },
            {
              "expressionType": "builder",
              "builderType": "objectCollection",
              "methods": [
                {
                  "method": "withStrings",
                  "arguments": [
                    {
                      "expressionType": "variable",
                      "name": "email"
                    }
                  ]
                }
              ]
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
      ]
    },
    {
      "id": 2,
      "name": "navigateToHomepage",
      "description": "Navigates back to the homepage",
      "returnType": "boolean",
      "statements": [
        {
          "statementType": "methodCall",
          "object": "stateTransitionsManagement",
          "method": "openState",
          "arguments": [
            {
              "expressionType": "literal",
              "valueType": "integer",
              "value": 3
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
      ]
    }
  ]
}
```