---
sidebar_position: 1
---

# State

The concept of a State in Brobot usually refers to a class with a Name enum, State objects, and 
possibly ActionOptions and other variables bundled together. State classes are 
included as Spring beans (@Component) and have getters (@Getter) for all variables. State
classes have only objects and their getters, and do not define methods.

There is also a State variable defined in the State class that contains many, if not all, 
of the objects included in the State class. It is this variable, and not the class, that
is manipulated by Brobot for state management. The State variable is added to the 
StateRepository and can be called by the StateService. A simple State class, containing
a State variable, looks like this:

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
    
        private Home(StateService stateService) { stateService.save(state); }
    }

Below is an example of a more complex State class. Notice that the State variable is passed
StateObjects but not the ActionOptions. ActionOptions are included in the State class
when they are reused. In this example, the same type of click,
a right click with a delay before the click, is used to make the State and its objects
visible. Another type of click is used to click on any of the State objects.  

    @Component
    @Getter
    public class OptionsMenu {
    
        public enum Name implements StateEnum {
            OPTIONS
        }
    
        private StateImageObject copyLink = new StateImageObject.Builder()
                .withImage("copyLink")
                .addSnapshot(new MatchSnapshot(500, 550, 40, 10))
                .build();
        private StateImageObject paste = new StateImageObject.Builder()
                .withImage("plakken")
                .addSnapshot(new MatchSnapshot(500, 500, 40, 10))
                .build();
    
        private State state = new State.Builder(Name.OPTIONS)
                .withImages(copyLink, paste)
                .build();
    
        private ActionOptions openOptions = new ActionOptions.Builder()
                .setAction(CLICK)
                .setClickType(ClickType.Type.RIGHT)
                .setPauseBeforeMouseDown(1)
                .setMaxWait(4)
                .build();
        private ActionOptions click = new ActionOptions.Builder()
                .setAction(CLICK)
                .setMaxWait(3)
                .build();
    
        public OptionsMenu(StateService stateService) { stateService.save(state); }
    }