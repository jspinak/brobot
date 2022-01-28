---
sidebar_position: 5
---

# Transitions

Transitions allow Brobot to move from one state to another. Any state that 
will be accessed needs a StateTransitions class.  

## Home Transitions

    @Component
    public class HomeTransitions {
    
        private final Action action;
        private final Home home;
    
        private StateTransitions transitions;
    
        public HomeTransitions(StateTransitionsRepository stateTransitionsRepository,
                               Action action, Home home) {
            this.action = action;
            this.home = home;
            transitions = new StateTransitions.Builder(HOME)
                    .addTransition(this::goToWorld, WORLD)
                    .build();
            stateTransitionsRepository.add(transitions);
        }
    
        private boolean goToWorld() {
            return action.perform(ActionOptions.Action.CLICK, home.getToWorldButton()).isSuccess();
        }
    
    }


## World Transitions

We use the builder for the transition from World to Island since it requires
a special option: in this transition the World state stays visible.  

    @Component
    public class WorldTransitions {
    
        private final Action action;
        private final World world;
    
        private StateTransitions transitions;
    
        public WorldTransitions(StateTransitionsRepository stateTransitionsRepository,
                                Action action, World world) {
            this.action = action;
            this.world = world;
            transitions = new StateTransitions.Builder(WORLD)
                    .addTransitionFinish(this::finishTransition)
                    .addTransition(new StateTransition.Builder()
                            .addToActivate(ISLAND)
                            .setFunction(this::goToIsland)
                            .setStaysVisibleAfterTransition(TRUE)
                            .build())
                    .build();
            stateTransitionsRepository.add(transitions);
        }
    
        private boolean finishTransition() {
            ObjectCollection worldImages = new ObjectCollection.Builder()
                    .withAllStateImages(world.getState())
                    .build();
            return action.perform(FIND, worldImages).isSuccess();
        }
    
        public boolean goToIsland() {
            ActionOptions clickTwice = new ActionOptions.Builder()
                    .setAction(ActionOptions.Action.CLICK)
                    .setTimesToRepeatIndividualAction(2)
                    .setPauseBetweenActions(.2)
                    .build();
            ObjectCollection searchButton = new ObjectCollection.Builder()
                    .withImages(world.getSearchButton())
                    .build();
            return action.perform(clickTwice, searchButton).isSuccess();
        }
    
    }

## Island Transitions

    @Component
    public class IslandTransitions {
    
        private final Action action;
        private final Island island;
    
        private StateTransitions transitions;
    
        public IslandTransitions(StateTransitionsRepository stateTransitionsRepository,
                                 Action action, Island island) {
            this.action = action;
            this.island = island;
            transitions = new StateTransitions.Builder(ISLAND)
                    .addTransitionFinish(this::finishTransition)
                    .build();
            stateTransitionsRepository.add(transitions);
        }
    
        private boolean finishTransition() {
            ObjectCollection worldImages = new ObjectCollection.Builder()
                    .withAllStateImages(island.getState())
                    .build();
            return action.perform(FIND, worldImages).isSuccess();
        }
    
    }
