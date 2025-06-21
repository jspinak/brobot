---
sidebar_position: 5
---

# StateRegion

`implements StateObject`  

A StateRegion belongs to a State and contains a Region that
has a special meaning for its owner State. For example, there
may be text in this Region that doesn't appear in any other State.  

## Fields (see StateObject for details)
`private String name`  

`private Region searchRegion`  

`private StateEnum ownerStateName`  

`private int staysVisibleAfterClicked`  
There may be a case where the expected contents of
a StateRegion change when it is clicked. This is however not yet implemented
as it is a rare condition, but can be set and used by an application if necessary.   

`private int probabilityExists` Also not yet implemented. The current version assumes 
StateRegions always exist.   

`private int timesActedOn`  

`private Position position`  

`private Anchors anchors`  

`private String mockText` No longer used and will be removed in the next version. 
This field was previously used, when not empty, to 
supersede the random generation of text by the mock GetText functions. The recommended
way now to set specific expected Strings is by adding GetText MatchSnapshots when 
creating the StateRegion.  

`private MatchHistory matchHistory`  

## Methods 
`public int x()` returns searchRegion.x.  

`public int y()` returns searchRegion.y.   

`public int w()` returns searchRegion.w.  

`public int h()` returns searchRegion.h.  

`public boolean defined()` returns true if 'searchRegion' is defined.  

`public void addTimesActedOn()` increments 'timesActedOn' by 1.  

`public void addSnapshot(MatchSnapshot matchSnapshot)`

## Builder Methods
`public Builder called(String name)`  

`public Builder withSearchRegion(Region searchRegion)`  

`public Builder inState(StateEnum stateName)`  

`public Builder setPointLocation(Position position)`  

`public Builder addAnchor(Position.Name definedRegionBorder, Position location)`  

`public Builder addSnapshot(MatchSnapshot matchSnapshot)`  

`public StateRegion build()`


