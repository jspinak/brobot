# Images Directory

Place your screenshot images here for pattern matching.

## Expected Structure:
```
images/
├── login-button.png
├── username-field.png
├── password-field.png
├── dashboard-logo.png
├── menu-button.png
├── submit-button.png
└── form/
    ├── username-field.png
    ├── password-field.png
    └── submit-button.png
```

## Note on Mock Mode
This example runs in mock mode by default (see application.yml).
In mock mode, Brobot simulates pattern matching without actual image files.

To use real GUI automation:
1. Set `brobot.core.mock: false` in application.yml
2. Capture screenshots of your target UI elements
3. Save them with the filenames shown above
4. Run the application

