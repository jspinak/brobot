---
sidebar_position: 3
---

# StateImageObject

`implements StateObject`  

StateImageObject represents an Image that exists in the owner State. Unless
it has shared images (that appear also in other States), this StateImageObject 
is representative of the State and will be
used to find it in case Brobot is lost. All StateImageObjects, including those with shared
images, are used to find a State during StateTransitions and with Find Actions. When a 
StateImageObject is found, its owner State is registered as an active State in 
StateMemory. 

StateImageObjects can have either an Image or a RegionImagePairs. RegionImagePairs are for
Images with fixed locations and are specified by the 'isFixed' option in the Builder.

## Fields
`private String name`   

`private StateEnum ownerStateName` is set by the State when the object is added to the State.  

`private int timesActedOn` is reset after each Action.  

`private MatchHistory matchHistory` is used to produce results during mock runs.  

`private SearchRegions searchRegionsObject` provides search regions for Find operations.
These regions can be superseded temporarily by the SearchRegions in an ActionOptions object.   

`private boolean fixed`  
When 'true', it activates the RegionImagePairs field. Otherwise, the 
Image field is used.    

`private Image image`  

`private RegionImagePairs regionImagePairs`  

`private int baseProbabilityExists`  
The probability that a StateImageObject will be found
can be influenced by events during a mock run. The base probability is used to reset the 
active probability value used with Find operations. Having lower base probabilities can
introduce more variability into the results. Probabilities are only used in the absence of
an applicable Match Snapshot in the MatchHistory.  

`private int probabilityExists` is the active probability used in Find operations.  

`private Position position` is used to convert a Match to a Location. When a Match is found,
the MatchObject created is passed this position to be available for clicking and other 
operations requiring a Location. The beginning value is `Position(50,50)`, or in the 
middle of the Match.  

`private boolean shared` specifies if the same image is used in other StateImageObjects. If 
so, it will be excluded from StateFinder operations, which attempt to find the set of 
active States after the current position is lost.  

`private Anchors anchors` are used for defining regions with the Match of this StateImageObject.    

## Methods  
`public Region getSearchRegion()` returns a single search region when needed.  

`public void setSearchRegion(Region region)`  

`public List<Region> getAllSearchRegions()`  

`public void setSearchRegionsObject(List<Region> regions)`  

`public void addSearchRegions(List<Region> regions)`  

`public boolean defined()` For fixed-location images, we assume that the StateImageObject
is found when one of the images in the RegionImagePairs is found. For variable-location 
images, the StateImageObject is defined when the Image's search region is explicitly 
defined.  

`public Optional<Region> getDefinedRegion()` returns an empty Optional when the result of
defined() is false. When the StateImageObject is defined, the appropriate Region is 
returned: for fixed-location images, the Region defined by the last Match; for 
variable-location images, the result of getSearchRegion(), which returns an explicitly-
defined search region.  

`public void setProbabilityToBaseProbability()`  

Adding anchors enables defining Regions with a Match from this StateImageObject.  
`public void addAnchor(Position.Name definedRegionBorder, Position positionInMatch)`  
`public void addAnchor(Position.Name definedRegionBorder, Position.Name positionInMatch)`   

`public ObjectCollection asObjectCollection()` is a convenience method that creates a new
ObjectCollection and add this StateImageObject to it as the only object.  

`public void addTimesActedOn()` increments the times acted on during a specific Action by 1.  

`public void addSnapshot(MatchSnapshot matchSnapshot)` adds a Snapshot to its MatchHistory.  

## Builder Methods
`public Builder called(String name)`  

`public Builder withSearchRegion(Region searchRegion)`  

`public Builder inState(StateEnum stateName)`  

`public Builder isFixed()`  

`public Builder withImage(String... imageNames)` adds a single Image with the given Patterns
specified by the imageNames parameter.  

`public Builder withImages(String... imageNames)` adds one Image per Pattern.  

`public Builder withImage(Image... images)` adds a single Image with all the Patterns 
from all the images passed to it.  

`public Builder withImages(Image... images)` adds the Images passed as parameters.  

`public Builder setBaseProbabilityExists(int prob)`  

`public Builder setProbabilityExists(int prob)`  

`public Builder withPosition(Position position)`  

`public Builder isShared()`  

`public Builder addAnchor(Position.Name borderOfRegionToDefine, Position positionInMatch)`  

`public Builder addAnchor(Position.Name borderOfRegionToDefine, Position.Name positionInMatch)`  

`public Builder addSnapshot(MatchSnapshot matchSnapshot)`   

`public StateImageObject build()`  


