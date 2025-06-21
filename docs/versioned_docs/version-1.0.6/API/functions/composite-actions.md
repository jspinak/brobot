---
sidebar_position: 2
---

# Composite Actions

Composite Actions use Basic Actions and Wrapper classes 
as building blocks to create more complex operations.  

All Actions follow the ActionInterface:

    Matches perform(
            ActionOptions actionOptions, 
            ObjectCollection... objectCollections);

Actions are presented here with their class names followed by their Action enums in parentheses.  

## Drag (DRAG)

Drags from one location to another on the screen.  

Both 'from' and 'to' locations can be an Image Match, Region, or Location.  
Uses a Basic Action (FIND) for both 'from' and 'to' locations and 
a Sikuli Wrapper (DragLocation).  
ObjectCollection #1 contains objects used to find the 'from' location and 
ObjectCollection #2 contains object used to find the 'to' location. 
Any ObjectCollections after #2 are ignored.  
[Drag-specific options](../data-types/actionOptions#dragtooffsetx--dragtooffsety) 
are DragToOffsetX and DragToOffsetY.  
Drag returns a Matches object with the results of the Find operation 
of ObjectCollection #2, as well as a DefinedRegion with (x,y) as the 
'from' Location and (x2,y2) as the 'to' Location.  

How the 'to' location is determined:  
If there are at least 2 ObjectCollections, 
the 'to' location is the result of a Find operation on ObjectCollection #2.  
If there is only 1 ObjectCollection, the results of a Find on this collection are used.  
If there are no ObjectCollections, the current mouse position is used.  
For a drag to occur with less than 2 ObjectCollections, the 'to' offsets cannot 
equal the 'from' offsets. The 'to' offsets are given by DragToOffsetX and
DragToOffsetY.  

Example: Drags starting from 10 pixels to the right of the 'from' location and 
ending at 20 pixels to the right of the 'to' location. The Find operations can 
last up to 2 seconds. If two ObjectCollections are provided, it will drag from 
the 10 pixels to the right of the match found in collection #1 
to 20 pixels to the right of the match found in collection #2. If one 
collection is provided, it will drag from 10 pixels to the right of 
that match to 20 pixels to the right of the same match.
If no collections are provided, it will drag from 10 pixels to the right of 
the current mouse position to 20 pixels to the right of the current position.  

        ActionOptions drag = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DRAG)
                .setMaxWait(2)
                .setAddX(10)
                .setDragToOffsetX(20)
                .build();

## ClickUntil (CLICK_UNTIL)

Clicks Match objects until a condition occurs or the operation times out.  

The actionOptions variable is used for both the CLICK and the 'until' Action.  
The two conditions currently available in the 
[ClickUntil-specific option 'ClickUntil'](../data-types/actionOptions#clickuntil) 
are OBJECTS_VANISH and OBJECTS_APPEAR. The default is OBJECTS_APPEAR.  

The Objects in the 1st ObjectCollection are acted on by the CLICK method.  
If there is a 2nd ObjectCollection, it is acted on by the FIND method.  
If there is only 1 ObjectCollection, the FIND method also uses these objects.  
In other words:
* 1 ObjectCollection: Click this until it disappears (applies only to OBJECTS_VANISH). 
* 2 ObjectCollections: Click #1 until #2 appears or disappears.

Example: Right-clicks on the objects in ObjectCollection #1 for a maximum of 
5 times until an object in collection #2 is found. The click position is 
3 pixels to the right of a match. The Find operations use a minimum similarity of
0.85, and there is a 0.4 second pause between the Find operation for collection #2
and the next Find operation for collection #1.

        ActionOptions clickUntil = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK_UNTIL)
                .setClickUntil(ActionOptions.ClickUntil.OBJECTS_APPEAR)
                .setMaxTimesToRepeatActionSequence(5)
                .setPauseBetweenActionSequences(.4)
                .setAddX(3)
                .setClickType(ClickType.Type.RIGHT)
                .setMinSimilarity(.85)
                .build();