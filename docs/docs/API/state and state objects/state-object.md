---
sidebar_position: 2
---

# StateObject

Classes implementing this Interface are StateImageObject, StateRegion, and StateLocation.  

MatchObjects are created for each match found during a Find operation, and include 
the StateObject that was found. It is often useful to be able to retrieve 
information about the StateObject that resulted in the MatchObject. Once a MatchObject
is created, the type of StateObject (Image/Region/Location) is no longer needed 
and the included object is of type StateObject. 

## Methods

`String getName()` is used for reporting and console output.   

`StateEnum getOwnerStateName()` is used primarily to update the active States in StateMemory 
during a Find operation.   

`Position getPosition()` is used to reference a single point in the MatchObject.   

`Anchors getAnchors()` is used by DefineRegion Actions to specify the point used in the 
MatchObject and the edges it defines in the Region being created.  

`int getTimesActedOn()`  
Knowing how many times an object's match was acted on can be 
valuable for understanding the actual automation as well as for performing mocks.  

`void addTimesActedOn()` increments the number of times the StateObject was acted on.   

`void setTimesActedOn(int times)` is used primarily to reset the times acted on for all
StateObjects before performing an Action.  

`void setProbabilityExists(int probabilityExists)`  
This method provides a way to change the probability the StateObject still
exists. It can be used in an application for more granular control of a mock run. 
You can change the probability that objects acted on are visible 
for future actions. For example, you may wish to simulate that an image 
disappears after being clicked.    
In relation to repetitions within Actions (examples are the ClickUntil Action and 
any Action with the ActionOptions timesToRepeatIndividualAction or
maxTimesToRepeatActionSequence set to a value greater than 1): 
This method could be used for simulating disappearance within an Action. It is not implemented
for this in the current version of Brobot because the preferred way of performing mocks
is with MatchHistories and not with probabilities. It can be used for future 
development in case development of probability-based mocking becomes a priority. This means
that in the current version, when mocking, clicking on an image match multiple times will 
occur each time if there is no MatchHistory. To simulate an object disappearing after a 
click, include a failed MatchSnapshot in the MatchHistory along with one or more 
successful MatchSnapshots. Since MatchSnapshots are chosen randomly, including a failed
Snapshot will make it likely that the StateObject disappears at some iteration of the 
operation.  

`MatchHistory getMatchHistory()` is used for producing mock results from MatchObjects.   

`void addSnapshot(MatchSnapshot matchSnapshot)`  
Snapshots are added to the MatchObject, 
if enabled, at the end of an Action. This occurs only during real execution and provides 
a real execution history that can be used for future mock runs. In the current version 
the MatchHistories will need to be processed by the application. Brobot does not save 
these to a database or to a file during execution. Due to this, the default value of 
BrobotSettings.saveSnapshots is false, and you will need to enable Snapshots as well as
save them in your code in order to collect them from real execution. 