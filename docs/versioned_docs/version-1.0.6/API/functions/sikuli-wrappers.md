---
sidebar_position: 3
---

# Wrappers

Wrapper classes serve as the interface between Brobot and other libraries that
perform real operations. The typical Brobot application will not call Wrappers directly. 

All Wrappers handle real and mock operations. If mocking is active, a 
mock will be performed. When mocking is not active, operations will
be carried out using modules that perform a real action. If the class is 
a Sikuli Wrapper, it will call Sikuli methods. If the class is another 
type of Wrapper, it will call some other module's methods that perform
real actions.

Classes above the Wrapper layer do not know if the application is returning
mocked or real results. 

# Sikuli Wrappers

## Find

### FindFirstPattern

Returns the results, contained in a Matches object, for the first Pattern found 
in a Brobot Image, which itself may contain multiple Patterns. 

`Matches find(Region region, StateImageObject stateImageObject, Image image,
ActionOptions actionOptions)`  

### FindAllPatterns

Finds all matches for all Patterns in the Brobot Image. When used by a 
Find.ALL operation, all matches are returned in the Matches object. When
used by a Find.BEST operation, only the match with the highest score is kept.

`Matches find(Region region, StateImageObject stateImageObject, Image image,
ActionOptions actionOptions)`

## Mouse

### ClickLocationOnce

Performs a full click once, with pauses, mouse down, and mouse up. 

`boolean click(Location location, ActionOptions actionOptions)`

### MouseDownWrapper

Presses and holds a mouse button. 

`boolean press(double pauseBeforeBegin, double totalPause, ClickType.Type type)`

### MouseUpWrapper

Releases a mouse button.

`boolean press(double pauseBefore, double pauseAfter, ClickType.Type type)`

### MouseWheel

Scrolls the mouse wheel up or down.

`boolean scroll(ActionOptions actionOptions)`

### MoveMouseWrapper

Moves the mouse to a given Location.

`boolean move(Location location)`

## Text

### GetTextWrapper

Finds text on-screen and stores it in the Matches object. 
Can find text for all Image matches or for a StateRegion.

`void allText(Matches matches, Image image)`
`void text(Matches matches, StateRegion stateRegion)`

### KeyDownWrapper

Holds a Key down. Special keys such as CTRL or ENTER are specified by an integer value.  

`void press(String key)`
`void press(int key)`

### KeyUpWrapper

Methods for releasing all keys or a specific key.

`void release()`
`void release(String key)`
`void release(int key)`

### TypeTextWrapper

Types a String to the focused window.

`boolean type(StateString stateString, ActionOptions actionOptions)`

## Other Functions

### App

Gets the window of the App in focus. When mocking is active, it will return a 
mock window. 

`Optional<Region> focusedWindow()`

### DragLocation

Drags from one Location to another Location.  

Typical settings:  
* 0.3 PauseBeforeMouseDown
* 0.3 PauseAfterMouseDown
* 0.5 MoveMouseDelay
* 0.4 PauseBeforeMouseUp
* 0.0 PauseAfterMouseUp

`boolean drag(Location from, Location to, ActionOptions actionOptions)`

### HighlightMatch

Highlights a Match with a given color. It's possible to turn on a highlight,
turn off a highlight, or show a highlight for a given number of seconds. 

`void turnOn(Match match, StateObject stateObject, ActionOptions actionOptions)`
`void turnOff(Match match)`
`boolean highlight(Match match, StateObject stateObject, ActionOptions actionOptions)`

### Wait

Pauses for a given number of seconds.

`void wait(double seconds)`

# Other Wrappers

### TimeWrapper

Wraps the Time functions to allow for mocking actions.
Keeps track of the mocked value of 'now' and performs mocked pauses.

<u>Variables</u>  

`private LocalDateTime now` keeps track of the current mock time.  

<u>Methods</u>  

`LocalDateTime now()`  
`void wait(double seconds)`  
`void wait(ActionOptions.Action action)`  
`void wait(ActionOptions.Find find)`  
`void printNow()`  
`void goBackInTime(double years, Object thingsYouWishYouCouldChange)`  

