---
sidebar_position: 2
---

# States

The state structure of Brobot attempts to simplify its environment by
emulating the structure of html pages. States are comprised of images and text
that all coexist at the same time, as well as regions,
locations, and strings that have a unique meaning here (for example,
clicking a location or typing a string produces a state-specific reaction).

The image below shows active states in different colors. 
Not all of these states would be needed for 
every application involving a finance website and a spreadsheet program, and 
simpler applications could get by with just a few states. In practice, there 
are usually multiple active states at any time. 
![States Example](/img/states3.png)

## State Management

The existence of states allows brobot to control process flow in a way
similar to the process flow in an html document. States are managed with 
the following processes:
- State-specific Transition classes define transitions to other states
  and finish any incoming transitions. [more on transitions](state-management#transitions)
- A Path Finder finds and sorts paths from active states to a target state. [more on paths](state-management#paths)
- State Memory keeps track of states currently visible. [more on state memory](state-management#state-memory)

## Cleaner and Simpler Code

States are defined as collections of state objects. No methods are included in state classes.
Each state has a state transition class, which contains only transition methods.
Moving from one state to another, finding the current state when lost, returning to the
process flow, and maintaining state memory are all handled by Brobot. The developer is free
to concentrate more on business logic and less on dealing with uncertainty and unexpected
situations.