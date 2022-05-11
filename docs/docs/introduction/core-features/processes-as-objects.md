---
sidebar_position: 5
---

# Processes as Objects

Interacting with the GUI is not as simple as 'find' and 'click'. There are a lot of
parameters involved. For example, a 'find' operation depends on settings such as the
minimum similarity required for the match to be accepted, the max amount of time to
search before giving up, and the region in which to search, as well as other parameters.
Building a library such as Brobot while using processes as functions would create either an
exploding web of methods or methods with extremely long parameter lists. It would be
unwieldy to use and complicated to develop.

Processes as objects has an additional advantage in that it allows a interactive process
to be treated as the combination of building blocks. This is important because brobot
allows for the development of complex applications that react to their environments,
in which the sequences of paths and actions taken are not known before the application
is run. 

A good example of how this has affected the design choices taken is how pauses
are used. Brobot makes a set of actions available to be used to create 
applications. Pause, or wait, is not one of them. Instead, pauses can be selected
as part of the action's options. 
Including pauses as options and not as a separate action was a deliberate design choice.
Having a pause method allows the programmer to think in a more procedural manner: for example,
do A and then wait a bit and then do B. Brobot's design incentivizes the
programmer to think about the process as discrete process objects that can be combined and
recombined in different configurations. Brobot provides a framework for a semi-intelligent
automation and not just for automating a static process flow.

Treating processes as objects also simplifies the creation of new composite actions out of the
more basic process building blocks. For example, the composite action ClickUntil, which performs
clicks until a condition is reached, is much simpler to create with the standard process
objects in Brobot.

The main classes enabling this are:
- ActionOptions: a collection of options that can be applied to any action (for example, min similarity).
  This object includes options for the action to take (i.e. FIND, CLICK, DRAG).
- ObjectCollection: a collection of state objects that are used with the action.
- Action: takes ActionOptions and ObjectCollections as parameters and executes the action specified
  in ActionOptions.
- Matches: contains all the results of the action linked to the corresponding objects in the
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
