---
sidebar_position: 3
---

# Configuration Schema

## Multiple ActionOptions Variables

An ActionOptions variable is included with every action. Some actions, like DRAG, 
require running multiple find operations (drag needs 
a starting and ending point). Currently, ActionOptions has options for modifying the 
ending point. The ActionOptions variable is the one point of contact for specifying options
on GUI actions and this is a good design choice. However, I can simplify it and make it more 
modular for more complex actions like DRAG by allowing actions to receive multiple ActionOptions
variables. Actions can already receive multiple ObjectCollection variables, which correspond to 
different points in DRAG, and the same concept can be applied to ActionOptions.