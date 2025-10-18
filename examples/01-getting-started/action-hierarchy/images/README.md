# Images Directory

Place your screenshot images here for pattern matching.

## Expected Structure for Action Hierarchy Examples:
```
images/
└── buttons/
    ├── next-button.png
    ├── finish-button.png
    └── submit-button.png
```

## Note on Mock Mode
This example runs in mock mode by default (see application.yml).
In mock mode, Brobot simulates pattern matching without actual image files.

## To Use Real GUI Automation:
1. Set brobot.core.mock: false in src/main/resources/application.yml
2. Capture screenshots of your target UI elements
3. Save them with the filenames shown above
4. Run the application with ./gradlew bootRun

## Capturing Screenshots:
- Use Brobot screenshot capture tool or your OS utility
- Capture only the UI element with a small margin
- Save as PNG format
- Use descriptive names matching StateImage patterns in ExampleState.java
