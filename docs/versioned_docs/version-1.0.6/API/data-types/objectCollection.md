---
sidebar_position: 2
---

# ObjectCollection

This class holds all of the objects that can be passed to an Action.  
It is created with a builder method. 

## Fields
`List<StateLocation> stateLocations`  
`List<StateImageObject> stateImages`  
`List<StateRegion> stateRegions`  
`List<StateString> stateStrings`  
`List<Matches> matches`  

## Methods 
`boolean empty()` returns true if the ObjectCollection contains no objects.  

`void resetTimesActedOn()` sets the timesActedOn variable to 0 for all
objects, including those found in the Matches variable. Knowing how many times
an object Match was acted on is valuable for understanding the actual automation as
well as for performing mocks.

## Builder Methods
`withLocations(Location... locations)`   
`withLocations(StateLocation... locations)`   
`withImages(Image... images)`   
`withImages(List<StateImageObject> images)`   
`withImages(StateImageObject... images)`   
`withAllStateImages(State state)`   
`withRegions(Region... regions)`   
`withRegions(StateRegion... regions)`   
`withGridSubregions(int rows, int columns, Region... regions)`   
`withGridSubregions(int rows, int columns, StateRegion... regions)`   
`withStrings(String... strings)`   
`withStrings(StateString... strings)`   
`withMatches(Matches... matches)`   
`build()` returns the new ObjectCollection.
