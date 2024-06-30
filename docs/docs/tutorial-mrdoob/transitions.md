---
sidebar_position: 5
---

# Transitions
 
The transitions classes do 2 things. They tell Brobot how to move from their state to another state, and also how
to finish an incoming transition to their state. These transitions are also saved with Brobot so that Brobot
can manage state transitions for us.

Here are the transitions classes:

      @Component
      public class HomepageTransitions {
      
          private final Action action;
          private final Homepage homepage;
      
          public HomepageTransitions(StateTransitionsRepository stateTransitionsRepository,
                                     Action action, Homepage homepage) {
              this.action = action;
              this.homepage = homepage;
              StateTransitions transitions = new StateTransitions.Builder("homepage")
                      .addTransitionFinish(this::finishTransition)
                      .addTransition(new StateTransition.Builder()
                              .addToActivate("harmony")
                              .setFunction(this::gotoHarmony)
                              .build())
                      .build();
              stateTransitionsRepository.add(transitions);
          }
      
          private boolean finishTransition() {
              return action.perform(FIND, homepage.getHarmony()).isSuccess();
          }
      
          private boolean gotoHarmony() {
              return action.perform(CLICK, homepage.getHarmony()).isSuccess();
          }
      
      }
      
      @Component
      public class HarmonyTransitions {
      
          private final Action action;
          private final Harmony harmony;
      
          public HarmonyTransitions(StateTransitionsRepository stateTransitionsRepository,
                                     Action action, Harmony harmony) {
              this.action = action;
              this.harmony = harmony;
              StateTransitions transitions = new StateTransitions.Builder("harmony")
                      .addTransitionFinish(this::finishTransition)
                      .addTransition(new StateTransition.Builder()
                              .addToActivate("about")
                              .setFunction(this::gotoAbout)
                              .build())
                      .build();
              stateTransitionsRepository.add(transitions);
          }
      
          private boolean finishTransition() {
              return action.perform(FIND, harmony.getAbout()).isSuccess();
          }
      
          private boolean gotoAbout() {
              return action.perform(CLICK, harmony.getAbout()).isSuccess();
          }
      
      }
      
      @Component
      public class AboutTransitions {
      
          private final Action action;
          private final About about;
      
          public AboutTransitions(StateTransitionsRepository stateTransitionsRepository,
                                    Action action, About about) {
              this.action = action;
              this.about = about;
              StateTransitions transitions = new StateTransitions.Builder("about")
                      .addTransitionFinish(this::finishTransition)
                      .build();
              stateTransitionsRepository.add(transitions);
          }
      
          private boolean finishTransition() {
              return action.perform(FIND, about.getAboutText()).isSuccess();
          }
      
      }
