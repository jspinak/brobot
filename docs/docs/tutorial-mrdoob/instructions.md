---
sidebar_position: 6
---

# Automation Instructions

What the automation will do depends on your automation goals. Our goal here is simply to test our model. 
We want to go from the homepage to the about text. Since we have a state structure, or model, Brobot can do this
for us. Let's create a class called AutomationInstructions and a method called doAutomation. This method will 
have one line of code that will tell Brobot to take us to the "about" state. Brobot does the rest for us.

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

