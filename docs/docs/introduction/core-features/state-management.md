---
sidebar_position: 7
---

# State Management

Brobot actively maintains an awareness of which States are active at any
time. It does this by keeping track of which States have become active,
which States have exited, and which States have been hidden as others have
become active. Paths and Transitions allow Brobot to move from one State to
another with one line of code. It is not important which States are currently
active, as Brobot can find a path to the target State and follow it.

## State Memory

The Find operation works together with the State Memory, which is
a class that holds the State currently active. When an image is found,
its owner State is registered in State Memory. Similarly, when a State
is exited, it is removed from State Memory.

## Hidden States

Imagine in the program you are controlling you have a menu that covers
part of the screen. When it is opened, it covers any States in this area,
and when it is closed these States reappear. Since the menu can be opened at
any time while the program is running, you don't know beforehand which
States will be hidden. The easiest path to one of the hidden States is
simply closing the menu, but how can Brobot know this? The answer is through
management of hidden States, which are
registered for each State when the State is opened, and removed from the
State's hidden States field when the State is closed. Transitions allow for
a variable State name called PREVIOUS to specify how to move to a hidden State.

## Transitions

Every State that is reachable needs to have an associated StateTransitions class.
The StateTransitions class comprises Transition objects, which provide a
means to move from one State to another. Transitions are split into two types:
FromTransitions and ToTransitions. FromTransitions handle the process of moving
from the current State to another State, and ToTransitions end the process of
moving to the current State. For example, to go from State A to State B, first
the FromTransition A -> B will be called, and then the ToTransition -> B will
be called. It looks like this:

<u>StateTransitions for State A</u>

The FromTransition tells us what to do in State A in order to go to State B

<u>StateTransitions for State B</u>

The ToTransition tells us what to do in State B to finish opening the State,
regardless of which State started the process. There can be multiple FromTransitions
going to State B from different States, but there is only one ToTransition to State B.

In the StateTransitions Builder, ToTransitions are created with the command 'addTransitionFinish'
and FromTransitions are created with the command 'addTransition'. The method passed to
'addTransitionFinish' is always called finishTransition(). The methods passed to
'addTransition' can have any name.

## Paths

Paths are State chains that show how to reach a target State from a start State.
Going to any State is as easy as writing 'open(State name)' and Brobot will
find Paths to that State and attempt to transition until the target State is reached
or all Paths have been unsuccessfully attempted. Paths have scores associated with
them, corresponding to the sum of the scores of individual States. Paths with lower
scores will be tried first, and State scores can be changed dynamically if necessary.

## State Finder and the Unknown State

In case Brobot gets lost and cannot find any of the active States, there is
a State Finder that will search for active States. This is a costly operation
but will rebuild the list of active States in State Memory and allow the
automation to continue.

There is also a possibility that something really unexpected happens and that
no States are active. In this case the UNKNOWN State will become active. The UNKNOWN
State also has Transitions and will attempt these Transitions in order to find its
way back to the target State. Any code meant to deal with unexpected situations
in which no States are visible should go into the UNKNOWN State's Transitions. 

