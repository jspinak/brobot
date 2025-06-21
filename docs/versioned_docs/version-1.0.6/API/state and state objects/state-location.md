---
sidebar_position: 6
---

# StateLocation

`implements StateObject`  

A StateLocation belongs to a State and contains a Location that
has a special meaning for its owner State. For example, clicking on
this Location has an effect in the owner State but not in other States.  

## Fields (see StateObject for details)
`private String name`  

`private Location location`  

`private StateEnum ownerStateName`  

`private int staysVisibleAfterClicked`  
There may be a case where the expected results of
clicking on a StateLocation change after it is clicked. This is however not yet implemented 
as it is a rare condition, but can be set and used by an application if necessary.  

`private int probabilityExists`  
Also not yet implemented. The current version assumes 
StateLocations always exist.  

`private int timesActedOn`  

`private Position position`  

`private Anchors anchors`  

`private MatchHistory matchHistory`  

## Methods 
`public boolean defined()` is true when 'location' is not null.   

`public void addTimesActedOn()` increments 'timesActedOn' on by 1.   

`public void addSnapshot(MatchSnapshot matchSnapshot)`   

## Builder Methods
`public Builder called(String name)`  

`public Builder withLocation(Location location)`  

`public Builder inState(StateEnum stateName)`  

`public Builder setAnchor(Position.Name cornerOfRegionToDefine)`  

`public StateLocation build()`