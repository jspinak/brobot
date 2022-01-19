---
sidebar_position: 3
---

# Matches

Matches holds the results of Actions. It is designed to keep track of 
the results of Actions and links these results to the causal State objects.

## Fields
`List<MatchObject> matchObjects`
contain the MatchObjects found during the operation.  

`List<MatchObject> nonoverlappingMatches` 
is a subset of matchObjects where overlapping MatchObjects are removed.  

`MatchObject bestMatch` is updated every time a MatchObject is added.
The getter for bestMatch returns an Optional.   

`List<StateEnum> activeStates` is a list of State names containing objects 
found during the associated Action. The Find action updates the State Memory with these States.
Shared Images are treated the same as non-shared Images in normal operation, 
since it is assumed that Brobot knows where it is. Shared Images are treated differently when the 
active State is lost and Images are searched with the StateFinder. With the StateFinder, shared 
Images are not used to find active States.  

`Text text` is a Text object that contains all Strings read from the screen.  

`String selectedText` is the String selected from the Text field as the most accurate representation
of the text on-screen.  

`Duration duration` is the overall time elapsed during the operation.

`boolean success` is determined differently for different operations. 
The user can modify the success condition.  

`List<Region> definedRegions` are saved for Define operations, 
which define the bounderies of a region or regions.  

`int maxMatches` limits the number of MatchObjects to save.  

`DanglingSnapshots danglingSnapshots` are the unfinished MatchSnapshots that have yet to
be added to their respective Images.  

## Methods 
`void add(MatchObject match)`  
`void addMatchObjects(StateImageObject stateImageObject, List<Match> matchList, double duration)`  
`void addAll(Matches matches)`   
`void addString(String str)`  
`void addDefinedRegion(Region region)`  
`List<Match> getMatches()`   
`List<Region> getMatchRegions()`  
`List<Location> getMatchLocations()`  
`Optional<Location> getBestLocation()`  
`Optional<MatchObject> getBestMatch()`  
`Region getDefinedRegion()`   
`int size()`  
`boolean isEmpty()`  
`void setTimesActedOn(int timesActedOn)`  
`void setDuration(Duration duration)`  
`void saveSnapshots()`  
`void print()`  