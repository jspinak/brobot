---
sidebar_position: 4
---

# SearchRegions

SearchRegions allows for multiple Regions to be associated with an Image. This 
could be useful when you are not sure where an Image may be but want to exclude
an area, or when the desired search area cannot be described by one rectangle.  

## Fields

`List<Region> regions` is initialized with an undefined region representing the screen.  

## Methods  
`public Region getSearchRegion()` return either the first defined region or the screen region,
if no regions are defined.  

`public void setSearchRegions(List<Region> searchRegions)`  

`public List<Region> getAllRegions()`  

`public void addSearchRegions(Region... searchRegions)`   

`public void addSearchRegions(List<Region> searchRegions)`  

`public boolean defined()` returns true if one of the regions is defined.   

`public SearchRegions getDeepCopy()`  
