---
sidebar_position: 3
---

# Processes as Objects

Interacting with the GUI is not as simple as 'find' and 'click'. There are a lot of
parameters involved. For example, a 'find' operation depends on settings such as the
minimum similarity required for the match to be accepted, the max amount of time to
search before giving up, and the region in which to search, as well as other parameters.
Building a library such as brobot while using processes as functions would create either an
exploding web of methods or methods with extremely long parameter lists. It would be
unwieldly to use and complicated to develop.

Processes as objects has an additional advantage in that it allows a interactive process
to be treated as the combination of building blocks. This is important because brobot
allows for the development of complex applications that react to their environments,
in which the sequences of paths and actions taken are not known before the application
is run. A good example of how this has affected the design choices taken is how pauses
are used. There is a set of basic actions designed to be used to create new applications.
Pause, or wait, is not one of them (pause is only available as a wrapper
function, meant to be used as part of newly constructed process objects). Pauses are then
associated with specific process objects, which may be combined and recombined to
form process flows. Take the pressing of a button, for example. There may be a minimum pause
necessary in which the mouse hovers over the button before the mouse click will register
correctly. When selecting a pause as one of the options of a 'click' action,
the user is deliberately associating that pause with the action. There will not be loose
pauses in the code that need to be cleaned up or that are meant for the wrong action.

Treating processes as objects also simplifies the creation of new composite actions out of the
more basic process building blocks. For example, the composite action DoUntil, which does
an action until a condition is reached, is much simpler to create with the standard process
objects in brobot.

The main classes enabling this are:
- ActionOptions: a collection of options that can be applied to an action (for example, min similarity).
  This object includes options for the action to take (i.e. FIND, CLICK, DRAG).
- ObjectCollection: a collection of state objects that are used with the action.
- Action: takes ActionOptions and ObjectCollections as parameters and executes the action specified
  in ActionOptions.
- Matches: contains all of the results of the action linked to the corresponding objects in the
  ObjectCollection.

An example (this defines a region around a match):
```
ActionOptions defineRegion = new ActionOptions.Builder()
.setAction(ActionOptions.Action.DEFINE)
.setDefineAs(ActionOptions.DefineAs.MATCH)
.setMinSimilarity(.6)
.setAddX(-5)
.setAddY(-1)
.setAbsoluteWidthOfDefinedRegion(296)
.setAbsoluteHeightOfDefinedRegion(255)
.build();

ObjectCollection mapImage = new ObjectCollection.Builder()
.withImages(miniMap.getMap())
.build();

Matches matches = action.perform(defineRegion, mapImage);
```
