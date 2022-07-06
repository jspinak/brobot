---
sidebar_position: 1
---

# ActionOptions

ActionOptions provides options for configuring an action. It can be used 
for all standard Brobot Actions, which include all BasicActions and some
CompositeActions. Every standard Action has a corresponding enum that can 
be selected in the Action field of ActionOptions.

## action
Type: `enum`

BasicActions:
* FIND
* CLICK
* DEFINE return a Region with a specific location and dimensions
* TYPE sends keyboard input
* MOVE moves the mouse
* VANISH is successful when an Image or State disappears
* GET_TEXT reads text from a Region
* HIGHLIGHT highlights a Match, Region, or Location
* SCROLL_MOUSE_WHEEL
* MOUSE_DOWN
* MOUSE_UP
* KEY_DOWN
* KEY_UP

CompositeActions:
* CLICK_UNTIL clicks Matches, Regions, and/or Locations until a condition is fulfilled
* DRAG

The default Action is FIND.

## FIND Options

### find
Type: `enum`

The Find enum specifies how a Find operation should be carried out.  
Keep in mind that ObjectCollections can contain multiple Images, and
Images can contain multiple Patterns (or image files).

* FIRST: first Match found
* EACH: one Match per Image
* ALL: all Matches for all Patterns in all Images
* BEST: the best match from all Patterns in all Images
* CUSTOM: user-defined.  
 Must be of type `BiFunction<ActionOptions, List<StateImageObject>, Matches>>`

The options that return multiple Matches allow for overlapping Matches.  
The default option is FIRST.

### tempFind
Type: `BiFunction<ActionOptions, List<StateImageObject>, Matches>`

A user-defined Find method to be used in a single operation.  
Find.CUSTOM is for user-defined Find methods meant to be reused.

### similarity
Type: `double`

Specifies how similar the found Match must be to the original Image. 
See the Sikuli documentation for detailed information.  
The default value is the Sikuli variable Settings.MinSimilarity.

### doOnEach
Type: `enum`

Images can contain multiple Patterns.  
DoOnEach specifies how Find.EACH should approach individual Images.

* FIRST: first Match on each Image
* BEST: best Match on each Image

The default value is FIRST.

## useDefinedRegion
Type: `boolean`

Instead of searching for a StateImageObject, use its defined Region to create a Match.
This is either the first found region if the StateImageObject is fixed, or the first 
defined Region in SearchRegions.  

## searchRegions
Type: `SearchRegions`

Sets temporary search regions that override the search regions of State objects
during a Find operation. 

## scrollDirection
Type: `enum`

* UP
* DOWN

For scrolling with the mouse wheel.  
The default value is DOWN.

## getTextUntil
Type: `enum`

* NONE: Text is searched for until the operation times out or the max number of iterations is reached.  
* TEXT_APPEARS: Keep searching for text until all Match regions have text.
* TEXT_VANISHES: Keep searching for text until no Match regions have text.

The default value is NONE.

## successEvaluation
Type: `Predicate<Matches>`

successEvaluation defines the success criteria for the Find operation.  
The default value is `matches -> !matches.isEmpty();`

## Pauses and Delays
The order that the different pauses work is shown in the example of a Drag operation:
1. pauseBeforeBegin
2. moveMouseDelay (to go to the drag point)
3. pauseBeforeMouseDown
4. pauseAfterMouseDown
5. moveMouseDelay (to go to the drop point)
6. pauseBeforeMouseUp
7. pauseAfterMouseUp
8. pauseAfterEnd

Pauses are always associated with actions: for example, pausing before clicking can increase
the chance that the click will be successful. There are also BrobotSettings for these options
that apply them to every action, but setting the pause options in ActionOptions
gives more granular control.

### pauseBeforeBegin
Type: `double`  
The default value is 0.

### moveMouseDelay
Type: `float`  
Delays the mouse movement. When set to 0 the mouse cursor will jump from the current
point to the target location.  
The default value is the Sikuli variable Settings.MoveMouseDelay.

### pauseBeforeMouseDown
Type: `double`  
The default value is the Sikuli variable Settings.DelayBeforeMouseDown.

### pauseAfterMouseDown
Type `double`  
The default value is BrobotSettings.delayAfterMouseDown.  
It replaces the Sikuli variable Settings.DelayBeforeDrag for Drag operations
and Settings.ClickDelay for Click operations.

### pauseBeforeMouseUp
Type: `double`  
The default value is the Sikuli variable Settings.DelayBeforeDrop.    
It replaces DelayBeforeDrop for Drag operations.

### pauseAfterMouseUp
Type: `double`  
The default value is 0.

### pauseAfterEnd
Type: `double`  
The default value is 0.

## dragToOffsetX & dragToOffsetY
Type: `int`

These values provide an offset to the Match for the dragTo Location.
To select the location to drag to, objects are chosen in this order:
  1. Objects in the 2nd ObjectCollection + offsets
  2. The dragFrom Location + offsets (when there is no 2nd ObjectCollection)

Other variables are used to adjust the dragFrom Location

## Click Options

### clickType
Type: `enum`

* LEFT
* RIGHT
* MIDDLE
* DOUBLE_LEFT
* DOUBLE_RIGHT
* DOUBLE_MIDDLE

### moveMouseAfterClick
Type: `boolean`

We have 2 options for moving the mouse after a click:
   1) To an offset of the click point
   2) To a fixed location 

If the offset is defined we move there; otherwise we move to the fixed location.
These options are also used for drags, and can move the mouse once the drag is finished.

### locationAfterClick
Type: `Location`

### offsetLocationBy
Type: `Location`

### clickUntil
Type: `enum`

Specifies the condition to fulfill after a Click.  
The Objects in the 1st ObjectCollection are acted on by the CLICK method.  
If there is a 2nd ObjectCollection, it is acted on by the FIND method.  
If there is only 1 ObjectCollection, the FIND method also uses these objects.  
_1 ObjectCollection: Click this until it disappears.  
2 ObjectCollections: Click #1 until #2 appears or disappears._

* OBJECTS_APPEAR
* OBJECTS_VANISH

The default value is OBJECTS_APPEAR.

## maxWait
Type: `double`

maxWait gives the max number of seconds to perform an operation.

## IndividualActions & ActionSequences

IndividualAction refers to individual activities, such as clicking on a single Match.
When clicking a Match, timesToRepeatIndividualAction gives the number of consecutive clicks
on this Match before moving on to the next Match.  
An ActionSequence refers to all activities in one iteration of a BasicAction, such as:
* clicking on all Matches of an Image
* clicking on a Match for each Pattern in a set of Images contained in an ObjectCollection

### timesToRepeatIndividualAction
Type: `int`  
Default value: 1

### maxTimesToRepeatActionSequence
Type: `int`  
Default value: 1

### pauseBetweenIndividualActions
Type: `double`  
Default value: 0

### pauseBetweenActionSequences
Type: `double`  
Default value: 0

## maxMatchesToActOn
Type: `int`  
Default value: 100

maxMatchesToActOn limits the number of Matches used when working with Find.ALL, Find.EACH,
Find.HISTOGRAM, and Find.COLOR. It is especially useful for working with HISTOGRAM and COLOR
Find operations since there could be potentially hundreds of thousands of matches.  

When <=0 it is not used.

## defineAs
Type: `enum`  
Default value: MATCH

* INSIDE_ANCHORS
* OUTSIDE_ANCHORS
* MATCH
* BELOW_MATCH
* ABOVE_MATCH
* LEFT_OF_MATCH
* RIGHT_OF_MATCH 
* FOCUSED_WINDOW

Anchors define Locations in Matches and specify how these Locations should be used
to define a Region (see the Anchor class for more info).  
INSIDE_ANCHORS defines the region as the smallest rectangle from the anchors found.  
OUTSIDE_ANCHORS defines the region as the largest rectangle from the anchors found.  
MATCH, BELOW_MATCH, ABOVE_MATCH, LEFT_OF_MATCH, RIGHT_OF_MATCH all define a Region
around a single Match.  
FOCUSED_WINDOW defines a Region around the active Window.

## Adjust Action Results

The following variables make adjustments to the final results of many actions.
For example, a Region defined as x.y.w.h = 10.10.50.50 will be
* 20.10.60.50 when addX = 10
* 10.10.60.50 when addW = 10
* 10.10.20.50 when absoluteW = 10
AbsoluteW and AbsoluteH are not used when set to <0.
When AbsoluteW is used, addW is not used. Same for H.

With the Drag operation, these variables are used for the dragFrom Location 
but not for the dragTo Location.

### addW
Type: `int`  
Default: 0

### addH
Type: `int`  
Default: 0

### absoluteW
Type: `int`  
Default: -1

### absoluteH
Type: `int`  
Default: -1

### addX
Type: `int`  
Default: 0

### addY
Type: `int`  
Default: 0

## Highlighting

### highlightAllAtOnce
Type: `boolean`  
Default: false

When set to true, all matches will be highlighted at once. When
set to false, matches will be highlighted one at a time.

### highlightSeconds
Type: `double`  
Default: 1

### highlightColor
Type: `String`  
Default: "red"

See the Sikuli method `region.highlight()` for more info.

## Typing

The below options are for typing characters to the active window.
Modifiers are used for key combinations such as 'SHIFT a' or 'CTRL ALT DEL'.
For more information on modifiers, see the Sikuli documentation.

### typeDelay
Type: `double`  
Default: Settings.TypeDelay

### modifiers
Type: `String`  
Default: ""  
Not used when ""  
Modifiers can be combined by adding them together.