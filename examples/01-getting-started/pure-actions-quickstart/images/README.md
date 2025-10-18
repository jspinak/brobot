# Images Directory

Place your screenshot images here for pattern matching.

## Expected Structure for Pure Actions Examples:
```
images/
├── submit-button.png
├── text-field.png
├── target-element.png
├── delete-option.png
├── multi-element.png
├── critical-button.png
├── save-button.png
├── button-pattern.png
├── login-button.png
├── username-field.png
├── password-field.png
└── confirm-dialog.png
```

## Note on Mock Mode
This example runs in mock mode by default (see application.yml).
In mock mode, Brobot simulates pattern matching without actual image files.

To use real GUI automation:
1. Set `brobot.core.mock: false` in application.yml
2. Capture screenshots of your target UI elements
3. Save them with the filenames shown above
4. Run the application with `./gradlew bootRun`

## Capturing Screenshots
The best way to capture screenshots for Brobot:
1. Use the Brobot screenshot capture tool or your OS screenshot utility
2. Capture only the UI element (button, field, etc.) with a small margin
3. Save as PNG format
4. Use descriptive names matching your StateImage patterns

