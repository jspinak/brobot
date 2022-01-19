---
sidebar_position: 1
---

# Basic Actions

Basic Actions are simple processes that require a maximum of 1 Find operation.  

All Actions follow the ActionInterface:  

    Matches perform(
            ActionOptions actionOptions, 
            ObjectCollection... objectCollections);

Actions are presented here with their class names followed by their Action enums in parentheses.  
Unless specifically stated, methods use only the objects in the first ObjectCollection.  

## Click (CLICK)

Clicks on an Image Match, Region, or Location.

[Click-specific options](../data-types/actionOptions#click-options) include:  
* ClickType
* MoveMouseAfterClick
* LocationAfterClick
* OffsetLocationsBy

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
* DefineAs: Provides various options for defining the region, including 
defining it around a match or as the smallest rectangle produced by a 
series of matches.

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

Finds Images and creates Match objects directly from Regions, Locations, and Matches.  

[Options](../data-types/actionOptions#find-options) include:  
* Find: Specifies whether the first match, the best match, one match per Image, or 
all matches should be returned. 
* TempFind: A user-defined find operation, meant to be used once, that could be added 
as a lambda. 
* Similarity: The minimum similarity score that is required to produce a match.
* DoOnEach: Used with the Find option EACH, DoOnEach specifies whether individual
Images should be searched for the first match or the best match.

Example #1: Since Find is the default Action, the standard Find can be created
without adding any options. 

        ActionOptions find = new ActionOptions();

The Action also can be called without an ActionOptions
variable if using a standard Find: `action.perform(objectCollections);`

Example #2: Search for a match for each of the Images in the 
ObjectCollection, with a maximum of 5 matches. For each max,
add a buffer on all sides of 10. Use a minimum similarity of 0.80
when searching, and time out if the operation lasts longer than
3 seconds. 

        ActionOptions find = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.EACH)
                .setAddX(-10)
                .setAddY(-10)
                .setAddW(20)
                .setAddH(20)
                .setMaxWait(3)
                .setMaxMatchesToActOn(5)
                .setMinSimilarity(.8)
                .build();

## MouseDown (MOUSE_DOWN)

Presses and holds a mouse button.  

The ObjectCollections are not used here.
The options used are [pauses](../data-types/actionOptions#pauses-and-delays) and 
[click type](../data-types/actionOptions#clicktype). An empty Matches object is returned 
and the operation is assumed to be successful.

Example: Wait 0.3 seconds before pressing the left mouse button, and 
pause 0.5 seconds with the button down. The button remains down when
the action is finished.

        ActionOptions mouseDown = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_DOWN)
                .setPauseBeforeMouseDown(.3)
                .setPauseAfterMouseDown(.5)
                .build();

## MouseUp (MOUSE_UP)

Releases a mouse button.  

The ObjectCollections are not used here.
The options used are [pauses](../data-types/actionOptions#pauses-and-delays) and
[click type](../data-types/actionOptions#clicktype). An empty Matches object is returned
and the operation is assumed to be successful.

Example: Release the right mouse button and pause for 0.4 seconds. If the right 
mouse button was not held down the only effect will be a 0.4 second pause.

        ActionOptions mouseUp = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOUSE_UP)
                .setClickType(ClickType.Type.RIGHT)
                .setPauseAfterMouseUp(.4)
                .build();

## MouseMove (MOVE)

Moves the mouse to one or more locations.   

There can be multiple points per ObjectCollection if Find.EACH or Find.ALL is used.  
There may be multiple ObjectCollections.  
Points are visited in the following order:
* In the order the ObjectCollection appears
* Within an ObjectCollection, as recorded by the Find operation:   
1) Images 2) Matches 3) Regions 4) Locations

Example: Move the cursor with a pause between moves of 0.2 seconds. The mouse
will not jump from one position to another but will move with a delay of 0.1 seconds
between points, which makes the total delay from one point to another of 0.3 seconds.
Each Image in all ObjectCollections will be searched. For each Image, the best
match for all Patterns will be used. The entire sequence will happen 2 times, meaning
each point will be visited twice. 

        ActionOptions move = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.MOVE)
                .setPauseBetweenActions(.2)
                .setMoveMouseDelay(.1F)
                .setFind(ActionOptions.Find.EACH)
                .doOnEach(ActionOptions.DoOnEach.BEST)
                .setMaxTimesToRepeatActionSequence(2)
                .build();

## ScrollMouseWheel (SCROLL_MOUSE_WHEEL)

Scrolls the mouse wheel up or down.

Example: Scroll up.

        ActionOptions scroll = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.SCROLL_MOUSE_WHEEL)
                .setScrollDirection(ActionOptions.ScrollDirection.UP)
                .build();

## GetText (GET_TEXT)

Retrieves text from a Region.    

[Options](../data-types/actionOptions#gettextuntil) include:
* Waiting for text to appear: the action terminates when all Match regions have text.
* Waiting for text to disappear: the action terminates when no Match regions have text.
If one of these conditions is not set, GetText will continue to find and save Strings in
its MatchObjects until it reaches the time or iteration limits set in ActionOptions.
One String, stored in the field 'selectedText', is chosen by the class TextSelector, 
which makes decisions about which String most likely captures the real text.  

Example: The objects in ObjectCollections will be searched for or converted into 
MatchObjects. A minimum similarity of .75 will be used when searching for Images.
For every MatchObject, its Match height will
be extended by 20. The Match regions will then be searched for text, no more
than five times, with a pause of 0.5 seconds in between text searches. The operation
will not last more than 3 seconds, and will stop once text has been found in 
every Match region.  

        ActionOptions getText = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.GET_TEXT)
                .setGetTextUntil(ActionOptions.GetTextUntil.TEXT_APPEARS)
                .setTimesToRepeatIndividualAction(5)
                .setMaxWait(3)
                .setAddH(20)
                .setPauseBetweenActions(.5)
                .setMinSimilarity(.75)
                .build();

## TypeText (TYPE)

Types text to the window in focus.  

Types all Strings in the first ObjectCollection. 
The option PauseBetweenIndividualActions gives the pause between typing Strings.
[Type-specific options](../data-types/actionOptions#typing) include:  
* TypeDelay sets the delay between characters.
* Modifiers are special keys such as CTRL and ALT that are held down during typing.
They can be combined by adding them together.

Example: With a delay between characters of 0.1 seconds, a pause between Strings
of 0.2 seconds, type all Strings in the first ObjectCollection with the 
SHIFT key held down. Pause for 1 second when this is finished, and then do all
of this again 1 more time. 

        ActionOptions type = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.TYPE)
                .setModifiers(Key.SHIFT)
                .setTypeDelay(.1)
                .setPauseBetweenActions(.2)
                .setPauseAfterEnd(1)
                .setMaxTimesToRepeatActionSequence(2)
                .build();

## Highlight (HIGHLIGHT)

Highlights found Images and other objects in the first ObjectCollection.  

[Highlight-specific options](../data-types/actionOptions#highlighting) include 
highlighting all at once or one at a time, 
highlight duration, and highlight color. Acceptable colors include
black, blue, cyan, gray, green, magenta, orange, pink, red, white, yellow, 
lightGray, and darkGray. For more colors refer to the Sikuli documentation.

Example: Highlight each Match one at a time in magenta for 2 seconds, 
with a 0.2 pause between highlights.

        ActionOptions highlight = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.HIGHLIGHT)
                .setHighlightAllAtOnce(false)
                .setHighlightSeconds(2)
                .setHighlightColor("magenta")
                .setPauseBetweenActions(.2)
                .build();

## WaitVanish (VANISH)

Waits for the Images to vanish.  

* Returns a successful Matches object if at some point no objects are found.  
* Returns an unsuccessful Matches object when at least one 
object exists for the entire wait period.  
* The Matches object will contain a MatchObject for each Image 
found the last time Find was successful. Therefore, checking if the
Matches object is empty will not give an indication of success or failure 
of the operation; as a rule, success should be ascertained with the 
isSuccess() method of the Matches object.  

Example: Searches the Images in the ObjectCollection with a minimum similarity of 0.9.
The operation is unsuccessful when one of the Images is still found at the end of 3 seconds
of searching. If at some point no Images are found, the operation is successful.

        ActionOptions vanish = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.VANISH)
                .setMinSimilarity(.9)
                .setMaxWait(3)
                .build();