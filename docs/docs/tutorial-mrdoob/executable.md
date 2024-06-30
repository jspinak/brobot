---
sidebar_position: 7
---

# The Executable Class

The main method in our executable class needs a few lines of code to initialize the application. 
First, we need to set up Spring Boot to work with a live GUI. This is done with the following lines:

     SpringApplicationBuilder builder = new SpringApplicationBuilder(MrdoobApplication.class);
     builder.headless(false);
     ConfigurableApplicationContext context = builder.run(args);

Then, we need to tell Brobot where to start. The addStateSet method tells Brobot that these states are likely to
be on-screen when the application starts. Brobot will identify which of these states is actually present at the start
of automation. The probabilities given here are for mocking and do not affect live automation. 

     // find initial active States
     InitialStates initialStates = context.getBean(InitialStates.class);
     initialStates.addStateSet(100, "homepage");
     initialStates.findIntialStates();

Lastly, we bring in the AutomationInstructions class, which serves as the start point for our automation, and run
the method doAutomation. 

     AutomationInstructions automationInstructions = context.getBean(AutomationInstructions.class);
     automationInstructions.doAutomation();

Alltogether, the executable class looks like this:

      @SpringBootApplication
      @ComponentScan(basePackages = {"io.github.jspinak.brobot", "com.example.mrdoob"})
      public class MrdoobApplication {
      
          public static void main(String[] args) {
              SpringApplicationBuilder builder = new SpringApplicationBuilder(MrdoobApplication.class);
              builder.headless(false);
              ConfigurableApplicationContext context = builder.run(args);
      
              // find initial active States
              InitialStates initialStates = context.getBean(InitialStates.class);
              initialStates.addStateSet(100, "homepage");
              initialStates.findIntialStates();
      
              AutomationInstructions automationInstructions = context.getBean(AutomationInstructions.class);
              automationInstructions.doAutomation();
          }
      
      }

