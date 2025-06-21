---
sidebar_position: 7
---

# StateString

A State String belongs to a State and contains a String that
has a special meaning for the owner State. For example, typing this
String may be part of a Transition for this State but not for other States.  

## Fields
`private String name`  

`private Region searchRegion`  
We may need to hover over or click on a 
region before typing the string. Not used by Brobot but can be used in an application.   

`private StateEnum ownerStateName`  

`private int timesActedOn`   

`private String string`   

## Methods 
`public boolean defined()` The StateString is defined if its string is neither null nor empty.  

`public static class InNullState` creates a StateString in the NULL State.   

`public void addTimesActedOn()` increments the times acted on by 1.  

## Builder Methods
`public Builder called(String name)`   

`public Builder withSearchRegion(Region searchRegion)`  

`public Builder inState(StateEnum stateName)`  

`public StateString build(String string)`   
