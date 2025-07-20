---
sidebar_position: 6
---

# Automation Instructions

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0. The original code examples are available in documentation versions 1.0.6 and 1.0.7.
:::

What the automation will do depends on your automation goals. Our goal here is simply to test our model. 
We want to go from the homepage to the about text. Since we have a state structure, or model, Brobot can do this
for us. Let's create a class called AutomationInstructions and a method called doAutomation. This method will 
have one line of code that will tell Brobot to take us to the "about" state. Brobot does the rest for us.

```java
@Component
public class AutomationInstructions {

    private final StateTransitionsManagement stateTransitionsManagement;

    public AutomationInstructions(StateTransitionsManagement stateTransitionsManagement) {
        this.stateTransitionsManagement = stateTransitionsManagement;
    }

    public void doAutomation() {
        stateTransitionsManagement.openState("about");
    }
}
```

