---
sidebar_position: 5
---

# Transitions

The most basic transition, clicking on an image match, can be created with
the State Structure builder by including the `~` tag in the filename, followed by 
part of the name of the state to transition to. 
If you have a long state name, you can abbreviate it, but make sure that this
abbreviation is not part of another state name; otherwise, the transition will not
be written. For example, `AnExampleState` could be abbreviated to `~xamp`, but if you
have another State called `ExampleState`, then you should use `~AnEx` instead or
just the whole State name `~AnExampleState`. If you don't have complex transitions,
you can let the builder write all of your transitions simply by including the
correct tag in the image filenames. It also writes PREVIOUS transitions with the tag
`~close` (PREVIOUS transitions deal with 
[hidden states](../../introduction/core-features/state-management#hidden-states) 
and provide for variable State transitions).
