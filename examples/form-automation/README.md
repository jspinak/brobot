# Form Automation Example

This example demonstrates advanced form automation techniques using Brobot, including:
- Field validation and error handling
- Dynamic form filling from data maps
- Tab navigation between fields
- Automatic logging for each form interaction
- Retry logic for unreliable forms

## Key Features

### 1. Automatic Logging
Every form interaction is automatically logged with descriptive messages:
- Before action: "Looking for First Name field..."
- Success: "Successfully entered data in First Name field"
- Failure: "Failed to find First Name field - check if form is loaded"

### 2. Smart Field Navigation
The example shows how to:
- Clear fields before typing (Ctrl+A, Delete)
- Use Tab to navigate between fields
- Handle dropdown selections
- Manage checkboxes and radio buttons

### 3. Data-Driven Approach
Forms are filled using a simple Map structure:
```java
Map<String, String> formData = Map.of(
    "firstName", "John",
    "lastName", "Doe",
    "email", "john.doe@example.com"
);
```

### 4. Error Recovery
Built-in retry logic handles:
- Fields that load slowly
- Validation errors
- Network delays

## Running the Example

1. Ensure you have a form application running that matches the field names in the example
2. Place screenshots of form fields in `src/main/resources/images/`
3. Run: `./gradlew bootRun`

## Project Structure

```
form-automation/
├── src/main/java/com/example/form/
│   ├── FormAutomationApplication.java    # Main Spring Boot application
│   ├── automation/
│   │   ├── FormFiller.java              # Core form filling logic
│   │   └── FormValidator.java          # Validation and verification
│   └── states/
│       ├── FormState.java               # Form state definition
│       └── ConfirmationState.java       # Success state definition
└── src/main/resources/
    ├── application.yml                   # Configuration
    └── images/                          # Form field screenshots
```

## Logging Output Example

```
[ACTION] Looking for First Name field...
[ACTION] Found First Name field at location (x: 150, y: 200)
[ACTION] Clicking First Name field...
[ACTION] Successfully clicked First Name field
[ACTION] Clearing field contents...
[ACTION] Field cleared
[ACTION] Typing John...
[ACTION] Successfully typed John
[ACTION] Form field completed in 523ms
```

## Customization

You can customize the form automation by:
1. Modifying field names in `FormState.java`
2. Adjusting timing in `application.yml`
3. Adding custom validation in `FormValidator.java`
4. Extending logging messages for your specific needs