```json
{
  "id": 1,
  "name": "FloraNext Automation",
  "description": "Example project for testing Brobot Runner with FloraNext website",
  "version": "1.0.0",
  "created": "2025-05-10T10:00:00Z",
  "updated": "2025-05-10T10:00:00Z",
  "states": [
    {
      "id": 1,
      "name": "Menu",
      "blocking": false,
      "pathScore": 1,
      "baseProbabilityExists": 100,
      "stateImages": [
        {
          "id": 101,
          "name": "FloraNextIcon",
          "shared": false,
          "patterns": [
            {
              "name": "floranext_icon",
              "imgPath": "images/menu-floranext_icon.png",
              "fixed": true,
              "searchRegions": [
                {"x": 10, "y": 10, "w": 200, "h": 50}
              ]
            }
          ]
        },
        {
          "id": 102,
          "name": "FloristWebsites",
          "patterns": [
            {
              "name": "florist_websites",
              "imgPath": "images/menu-florist_websites.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 103,
          "name": "FloristWebsitesSelected",
          "patterns": [
            {
              "name": "florist_websites_selected",
              "imgPath": "images/menu-florist_websites_selected.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 104,
          "name": "PointOfSale",
          "patterns": [
            {
              "name": "point_of_sale",
              "imgPath": "images/menu-point_of_sale.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 105,
          "name": "Pricing",
          "patterns": [
            {
              "name": "pricing",
              "imgPath": "images/menu-pricing.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 106,
          "name": "PricingSelected",
          "patterns": [
            {
              "name": "pricing_selected",
              "imgPath": "images/menu-pricing_selected.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 107,
          "name": "Resources",
          "patterns": [
            {
              "name": "resources",
              "imgPath": "images/menu-resources.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 108,
          "name": "StartForFree",
          "patterns": [
            {
              "name": "start_for_free",
              "imgPath": "images/menu-start_for_free.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 109,
          "name": "ToggleMenu",
          "patterns": [
            {
              "name": "toggle_menu",
              "imgPath": "images/menu-toggle_menu.png",
              "fixed": true
            }
          ]
        },
        {
          "id": 110,
          "name": "WeddingsAndEvents",
          "patterns": [
            {
              "name": "weddings_and_events",
              "imgPath": "images/menu-weddings_and_events.png",
              "fixed": true
            }
          ]
        }
      ]
    },
    {
      "id": 2,
      "name": "Pricing",
      "blocking": false,
      "pathScore": 1,
      "baseProbabilityExists": 100,
      "stateImages": [
        {
          "id": 201,
          "name": "PricingStartForFree",
          "patterns": [
            {
              "name": "pricing_start_for_free",
              "imgPath": "images/pricing-start_for_free.png",
              "fixed": true,
              "searchRegions": [
                {"x": 400, "y": 300, "w": 200, "h": 50}
              ]
            }
          ]
        }
      ]
    },
    {
      "id": 3,
      "name": "Homepage",
      "blocking": false,
      "pathScore": 1,
      "baseProbabilityExists": 100,
      "stateImages": [
        {
          "id": 301,
          "name": "EnterYourEmail",
          "patterns": [
            {
              "name": "enter_your_email",
              "imgPath": "images/enter_your_email.png",
              "fixed": true,
              "searchRegions": [
                {"x": 300, "y": 400, "w": 300, "h": 50}
              ]
            }
          ]
        },
        {
          "id": 302,
          "name": "StartForFreeBig",
          "patterns": [
            {
              "name": "start_for_free_big",
              "imgPath": "images/start_for_free_big.png",
              "fixed": true,
              "searchRegions": [
                {"x": 500, "y": 400, "w": 200, "h": 60}
              ]
            }
          ]
        }
      ]
    }
  ],
  "stateTransitions": [
    {
      "id": 1,
      "sourceStateId": 3,
      "stateImageId": 105,
      "actionDefinition": {
        "steps": [
          {
            "actionOptions": {
              "action": "CLICK",
              "clickType": "LEFT"
            },
            "objectCollection": {
              "stateImages": [105]
            }
          }
        ]
      },
      "staysVisibleAfterTransition": "TRUE",
      "statesToEnter": [2],
      "statesToExit": [3],
      "score": 1
    },
    {
      "id": 2,
      "sourceStateId": 2,
      "stateImageId": 101,
      "actionDefinition": {
        "steps": [
          {
            "actionOptions": {
              "action": "CLICK",
              "clickType": "LEFT"
            },
            "objectCollection": {
              "stateImages": [101]
            }
          }
        ]
      },
      "staysVisibleAfterTransition": "TRUE",
      "statesToEnter": [3],
      "statesToExit": [2],
      "score": 1
    }
  ],
  "automation": {
    "buttons": [
      {
        "id": "signUp",
        "label": "Sign Up",
        "tooltip": "Navigate to pricing and sign up for FloraNext",
        "functionName": "navigateToPricingAndSignUp",
        "parameters": {
          "email": "test@example.com"
        },
        "category": "Testing",
        "icon": "register",
        "position": {
          "row": 0,
          "column": 0,
          "order": 1
        },
        "styling": {
          "backgroundColor": "#4CAF50",
          "textColor": "white",
          "size": "medium"
        }
      },
      {
        "id": "goHome",
        "label": "Go to Homepage",
        "tooltip": "Navigate back to the homepage",
        "functionName": "navigateToHomepage",
        "category": "Testing",
        "position": {
          "row": 0,
          "column": 1,
          "order": 2
        }
      }
    ]
  },
  "configuration": {
    "minSimilarity": 0.8,
    "moveMouseDelay": 0.3,
    "delayBeforeMouseDown": 0.2,
    "delayAfterMouseDown": 0.2,
    "delayBeforeMouseUp": 0.2,
    "delayAfterMouseUp": 0.2,
    "typeDelay": 0.1,
    "pauseBetweenActions": 0.5,
    "maxWait": 10,
    "imageDirectory": "images",
    "logLevel": "INFO",
    "illustrationEnabled": true
  }
}
```