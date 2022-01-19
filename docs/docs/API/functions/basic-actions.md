---
sidebar_position: 1
---

# Basic Actions

All actions follow the ActionInterface:  
`Matches perform(ActionOptions actionOptions, ObjectCollection... objectCollections)`  

Class names are followed by the (Action enum)

## Click (CLICK)

Clicks on an Image Match, Region, or Location.

[Click-specific options](../data-types/actionOptions#click-options) include:  
* ClickType
* MoveMouseAfterClick
* LocationAfterClick
* OffsetLocationsBy
* StateProbabilitiesAfterClick

Example: Do a right mouse click, move the mouse after clicking to an 
offset of (30,50) from the click point, repeat the click 3 times, and 
pause for 1/2 second between clicks (between the mouse move and the next
click).

        ActionOptions click = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.CLICK)
                .setClickType(ClickType.Type.RIGHT)
                .setMoveMouseAfterClick(true)
                .setLocationAfterClickByOffset(30, 50)
                .setTimesToRepeatIndividualAction(3)
                .setPauseBetweenActions(.5)
                .build();

## DefineRegion (DEFINE)

Defines a Region as specified in the ActionOptions. 

[Define-specific options](../data-types/actionOptions#defineas) include:
* DefineAs

Example: Define a new Region around the Match with a minimum similarity
of 0.6, subtract (-5,-1) from the (x,y) of the Match and set the 
width and height of the Region to 296 and 255.

        ActionOptions defineRegion = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.DEFINE)
                .setDefineAs(ActionOptions.DefineAs.MATCH)
                .setMinSimilarity(.6)
                .setAddX(-5)
                .setAddY(-1)
                .setAbsoluteWidthOfDefinedRegion(296)
                .setAbsoluteHeightOfDefinedRegion(255)
                .build();

## Find (FIND)



## MouseDown (MOUSE_DOWN)

## MouseUp (MOUSE_UP)

## MouseMove (MOVE)

## ScrollMouseWheel (SCROLL_MOUSE_WHEEL)

## GetText (GET_TEXT)

## KeyDown (KEY_DOWN)

## KeyUp (KEY_UP)

## TypeText (TYPE)

## Highlight (HIGHLIGHT)

## WaitVanish (VANISH)

