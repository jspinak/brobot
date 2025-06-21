---
sidebar_position: 6
---

# Controlling the GUI

Brobot interacts with the GUI using the Sikuli library. This is done with
3 main levels of abstraction:

## Sikuli Wrappers

These are the methods that form the interface between Brobot and Sikuli.
Sikuli Wrappers route the operational instructions either to Sikuli methods,
which control the mouse and keyboard and capture data from the screen, or to
functions that mock (simulate) these methods. When calling Sikuli methods,
the Wrappers convert Brobot data types to Sikuli data types.  
[See in detail](../../API/functions/sikuli-wrappers)

## Basic Actions

Comprising Actions such as Find and Click, Basic Actions are simple processes
that require a maximum of 1 Find operation. Find operations are pretty
powerful and can return Matches for Images with a variety of search options.
For example, the DefineInsideAnchors class uses a Find Action to return a Match
for each object in the ObjectCollection, and then uses these Match objects to
define a Region. All of this is done with 1 Find operation.  
[See in detail](../../API/functions/basic-actions)

## Composite Actions

Composite Actions use Basic Actions and Sikuli Wrappers as building blocks
to create more complex operations. A typical candidate for a Composite Action
is a process that requires more than 1 Find operation, although Composite
Actions also could be made of multiple Basic Actions that require no Find
operations. For a Composite Action, achieving its goal with only 1 Find
operation is either not possible or would make the operation too confusing
or too complex. Examples of Composite Actions are ClickUntil and Drag.  
[See in detail](../../API/functions/composite-actions)