---
sidebar_position: 4
---

# States

Now we can define our states. Each state will have a state name and one image. After creating the 
state, we save it with Brobot so that Brobot knows it is part of our model and can manage it for us.

Here are our 3 state classes:

      @Component
      @Getter
      public class Homepage {
      
          private StateImage harmony = new StateImage.Builder()
                  .addPattern("harmonyIcon")
                  .build();
      
          private State state = new State.Builder("homepage")
                  .withImages(harmony)
                  .build();
      
          public Homepage(AllStatesInProjectService stateService) {
              stateService.save(state);
          }
      }

      @Component
      @Getter
      public class Harmony {
      
          private StateImage about = new StateImage.Builder()
                  .addPattern("aboutButton")
                  .build();
      
          private State state = new State.Builder("harmony")
                  .withImages(about)
                  .build();
      
          public Harmony(AllStatesInProjectService stateService) {
              stateService.save(state);
          }
      }

      @Component
      @Getter
      public class About {
      
         private StateImage aboutText = new StateImage.Builder()
                 .addPattern("aboutText")
                 .build();
      
         private State state = new State.Builder("about")
                 .withImages(aboutText)
                 .build();
      
         public About(AllStatesInProjectService stateService) {
            stateService.save(state);
         }
      }
