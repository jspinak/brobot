# Brobot Runner Example Configurations

This directory contains example configurations demonstrating various features and patterns of Brobot Runner. Each example is designed to teach specific concepts and can be used as a starting point for your own automations.

## Examples Overview

### 01-simple-click.json
**Level**: Beginner  
**Concepts**: Basic clicking, states, transitions

A minimal example showing how to:
- Define states with images
- Perform simple click actions
- Create transitions between states
- Add basic logging

**Use Case**: Clicking buttons in a simple application

### 02-form-filling.json
**Level**: Intermediate  
**Concepts**: Text input, keyboard navigation, variables

Demonstrates:
- Using variables for dynamic content
- Typing text into form fields
- Keyboard navigation (Tab, Ctrl+A)
- Field-by-field form completion
- Error handling for missing elements

**Use Case**: Automated form submission

### 03-conditional-flow.json
**Level**: Intermediate  
**Concepts**: Conditional logic, branching, state detection

Shows how to:
- Check current application state
- Make decisions based on image presence
- Handle optional UI elements (popups, notifications)
- Create robust login flows
- Implement if/then/else logic

**Use Case**: Smart navigation with decision making

### 04-loop-processing.json
**Level**: Intermediate  
**Concepts**: Loops, batch processing, counters

Covers:
- While loops with conditions
- For loops with fixed iterations
- Nested loops for complex processing
- Break statements for early exit
- Counter variables and increments

**Use Case**: Processing lists of items in batches

### 05-advanced-workflow.json
**Level**: Advanced  
**Concepts**: Modules, error recovery, parallel execution

Advanced features including:
- Importing reusable modules
- Try/catch/finally blocks
- Parallel action execution
- Complex error recovery strategies
- Report generation and export
- API integration
- User prompts and choices

**Use Case**: Enterprise-grade automation workflow

## Getting Started

1. **Load an Example**
   - Open Brobot Runner
   - File > Open Configuration
   - Navigate to the examples directory
   - Select an example file

2. **Required Images**
   - Examples reference images in an `images/` subdirectory
   - You'll need to capture your own images for your target application
   - Use descriptive names matching those in the examples

3. **Customize Variables**
   - Open the Variables panel
   - Modify values to match your needs
   - Variables are highlighted with `${}` syntax

4. **Run in Debug Mode**
   - Press F6 or Run > Debug
   - Step through actions to understand flow
   - Check logs for detailed information

## Common Patterns

### Error Handling Pattern
```json
{
  "type": "conditional",
  "condition": {
    "type": "imageExists",
    "target": "error_dialog"
  },
  "ifTrue": [
    {
      "type": "screenshot",
      "path": "errors/error_${timestamp}.png"
    },
    {
      "type": "click",
      "target": "error_close_button"
    }
  ]
}
```

### Retry Pattern
```json
{
  "type": "loop",
  "maxIterations": 3,
  "continueOnError": true,
  "actions": [
    {
      "type": "click",
      "target": "submit_button"
    },
    {
      "type": "wait",
      "condition": {
        "type": "imageAppears",
        "target": "success_message",
        "timeout": 5
      }
    }
  ]
}
```

### State Verification Pattern
```json
{
  "type": "conditional",
  "condition": {
    "type": "allConditions",
    "conditions": [
      {"type": "imageExists", "target": "expected_screen"},
      {"type": "imageNotExists", "target": "error_indicator"}
    ]
  },
  "ifTrue": [
    {"type": "log", "message": "State verified, proceeding"}
  ],
  "ifFalse": [
    {"type": "error", "message": "Unexpected state"}
  ]
}
```

## Best Practices

1. **Start Simple**
   - Begin with example 01 and work your way up
   - Test each action before adding complexity

2. **Use Descriptive Names**
   - Name states and images clearly
   - Add descriptions to complex actions

3. **Handle Errors Gracefully**
   - Always consider what could go wrong
   - Add recovery actions for common failures

4. **Optimize Performance**
   - Limit search regions for faster recognition
   - Cache frequently used images
   - Use appropriate similarity thresholds

5. **Document Your Logic**
   - Add comments using description fields
   - Explain complex conditional logic
   - Note any assumptions or requirements

## Creating Your Own

1. **Copy a Template**
   - Start with the example closest to your needs
   - Save with a new name

2. **Capture Images**
   - Use Brobot Runner's image capture tool
   - Ensure consistent lighting and resolution
   - Include unique identifying features

3. **Test Incrementally**
   - Validate each section before proceeding
   - Use debug mode to verify behavior
   - Check logs for warnings

4. **Refine and Optimize**
   - Adjust similarity thresholds as needed
   - Add error handling where appropriate
   - Consider edge cases

## Troubleshooting Examples

**Issue**: "Image not found" errors
- **Solution**: Recapture images from your application
- Ensure image paths are relative to the configuration file

**Issue**: Actions execute too quickly
- **Solution**: Add delays in settings or between actions
- Use wait conditions instead of fixed delays

**Issue**: Variable not recognized
- **Solution**: Check variable definition in the variables section
- Ensure proper `${}` syntax

## Additional Resources

- [Configuration Format Documentation](../docs/CONFIG_FORMAT.md)
- [User Manual](../docs/USER_MANUAL.md)
- [Troubleshooting Guide](../docs/TROUBLESHOOTING.md)
- [Brobot Wiki](https://github.com/jspinak/brobot/wiki)

## Contributing Examples

If you create useful examples, consider contributing them:
1. Follow the naming convention: `##-description.json`
2. Include clear documentation
3. Test thoroughly
4. Submit a pull request

Happy automating!