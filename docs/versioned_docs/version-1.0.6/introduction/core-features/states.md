---
sidebar_position: 2
---

# States

## What is a Brobot State

A state in Brobot is a collection of related objects, including images, regions,
locations, and strings. This 
relationship usually involves space (objects often are grouped together spatially) 
and time (objects often appear at the same time). These are, however, not fixed 
rules, as you can see in the example below where state objects are spread out 
over the entire screen. The one truly defining characteristic
of a state is the reliability of expected results:
when a state is active,
a specific action configuration performed on a state object should give the
same expected results every time. 

Below is an example of a state in a mobile game. The state holds 5 objects: 1 region, 
1 location, and 3 images. Clicking on an image, for example "Raid", should always produce
the same results when in this state. Similarly, clicking on the location gives us 
the same expected results in this state, namely that the island should be deselected. 
The region will hold the name of the current island. When this state is inactive, the 
same region would not give us the same expected results since it might capture text 
unrelated to an island name or no text at all.  

![island state](/img/island-state.jpeg)

Here is another example that shows a collection of states that are active 
at the same time (each state is in a different color).
States may transition to other states without changing the other active states.
This is another way to conceptualize state boundaries. Think of what might
change as a group and what might not change. Any objects that would change as
a group should be included in the same state.   

Not all of these states would be needed for 
every application involving a finance website and a spreadsheet program, and 
simpler applications could get by with just a few states. In practice, there 
are usually multiple active states at any time.

![States Example](/img/states3.png)

## The State Structure

The state structure is a way of organizing an environment into manageable parts.
Think of how simple html pages are structured, with elements belonging to different
pages, and transitions moving from one page to another. The same concept applies to
Brobot states, except that in a non-controlled environment the transitions can be
much more complex.

Below is a typical state structure created by the state structure builder. Each folder
has two classes: one for the state, holding the state objects, and one for state 
transitions, with code enabling movement to other states. 

![filestructure](/img/state-structure-filestructure.png)

## State Management

The existence of states allows Brobot to control process flow.
States are managed with 
the following processes:
- State-specific Transition classes define transitions to other states
  and finish any incoming transitions. [more on transitions](state-management#transitions)
- A Path Finder finds and sorts paths from active states to a target state. [more on paths](state-management#paths)
- State Memory keeps track of states currently visible. [more on state memory](state-management#state-memory)

Brobot takes care of movement from one state to another. This allows for modular
development, in which new processes or new applications can be created quickly
from the existing state structure. Once your state structure has been created, 
moving to a state is one line of code: `stateTransitionsManagement.open(STATE_TO_OPEN);`

## Cleaner and Simpler Code

States are defined as collections of state objects. No methods are included in state classes.
Each state has a state transition class, which contains only transition methods.
Moving from one state to another, finding the current state when lost, returning to the
process flow, and maintaining state memory are all handled by Brobot. The developer is free
to concentrate more on business logic and less on dealing with uncertainty and unexpected
situations.