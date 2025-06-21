---
sidebar_position: 7
title: 'Dynamic Transitions'
---

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