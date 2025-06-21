---
sidebar_position: 3
title: 'States'
---

## What is a Brobot State?

A state in Brobot is a collection of related objects, including images, regions, and locations. This relationship usually involves space (objects are often grouped together) and time (objects often appear together). The defining characteristic of a state is the reliability of expected results: when a state is active, a specific action performed on one of its objects should give the same expected result every time.

In the formal model, a **State (S)** is a collection of related GUI elements chosen to model a distinct configuration of the user interface.

Below is an example of a state in a mobile game. The state holds 5 objects: 1 region, 1 location, and 3 images. Clicking on the image "Raid" should always produce the same result when in this state.

![island state](/img/island-state.jpeg)

### Multiple Active States

In practice, there are usually multiple active states at any time. A key concept in the model is that the visible screen can be described as a set of active states S<sub>Ξ</sub>. States can transition independently without affecting other active states. When designing your automation, think of what might change as a group and what might not; objects that change together should be included in the same state.

The example below shows a screen with multiple states active simultaneously, each highlighted in a different color.

![States Example](/img/states3.png)