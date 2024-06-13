---
sidebar_position: 4
---

# States

There are 3 states in the demo: HOME, WORLD, and ISLAND. Each state describes a collection
of static images. The images are static in that they don't change their appearance
and always appear when the state is active. A state image's location on-screen may or may not change.

A state has an enum called Name that is used throughout the application to refer to the state. Every state
starts with an enum declaration.

## Home

The snapshot defined with the StateImageObject toWorldButton gives the location we 
expect to find this object. Snapshots represent possible locations for the image, and 
when there is more than one a match will be chosen randomly from all snapshots.   

When a StateImageObject is fixed, it is expected to be found in the same location every time.  

    @Component
    @Getter
    public class Home {
    
        public enum Name implements StateEnum {
            HOME
        }
    
        private StateImageObject toWorldButton = new StateImageObject.Builder()
                .withImage("toWorldButton")
                .isFixed()
                .addSnapshot(new MatchSnapshot(220, 600, 20, 20))
                .build();
    
        private State state = new State.Builder(HOME)
                .withImages(toWorldButton)
                .build();
    
        public Home(StateService stateService) { stateService.save(state); }
    }

## World

If a StateImageObject has no snapshots, the locations of matches will be
determined using probabilities. 

    @Component
    @Getter
    public class World {
    
        public enum Name implements StateEnum {
            WORLD
        }
    
        private StateImageObject searchButton = new StateImageObject.Builder()
                .withImage("searchButton")
                .isFixed()
                .build();
    
        private State state = new State.Builder(Name.WORLD)
                .withImages(searchButton)
                .build();
    
        public World(StateService stateService) { stateService.save(state); }
    }

## Island

Images can be defined with multiple image files.  

The action GetText references only GetText snapshots. Every snapshot
has an action associated with it; default snapshots (without an explicitly defined
action) are built as the result of Find operations. A GetText snapshot for a StateRegion
gives the expected text to find when performing a GetText action on the StateRegion. 
In our GetText snapshot, we have included a few different expected text results. Some of
them are misspelled to simulate the stochasticity of real execution, in which text is 
not always found as it appears on-screen.  

    @Component
    @Getter
    public class Island {
    
        public enum Name implements StateEnum {
            ISLAND
        }
    
        private StateImageObject islandName = new StateImageObject.Builder()
                .withImage("castle", "mines", "farms", "forest", "mountains", "lakes")
                .called("island type text")
                .isFixed()
                .build();
        private StateRegion islandRegion = new StateRegion.Builder()
                .called("island region")
                .addSnapshot(new MatchSnapshot.Builder()
                        .setActionOptions(ActionOptions.Action.GET_TEXT)
                        .addString("Mines")
                        .addString("Lakess")
                        .addString("Farmz")
                        .build())
                .build();
    
        private State state = new State.Builder(Name.ISLAND)
                .withImages(islandName)
                .withRegions(islandRegion)
                .build();
    
        public Island(StateService stateService) { stateService.save(state); }
    }
