---
sidebar_position: 1
---

# Building The State Structure
Brobot version 1.0.2+
Requires the dependency JavaPoet, which write Java code.  
In Gradle: `implementation group: 'com.squareup', name: 'javapoet', version: '1.9.0'`

## What is the State Structure

The State Structure is the collection of State and StateTransitions classes
that defines your environment and allows Brobot to move around it.

## Automate Building the State Structure

Brobot can build the State Structure for you. To do this, it takes and saves 
a collection of screenshots while you are working in your target environment and 
saves these screenshots to a project folder. With the screenshots, you cut out 
images that you wish to use as StateImageObjects and save them in the images folder. 
Naming these files is very important as it helps to determine your State Structure. 

## Benefits of Automating the State Structure

Automating the building of a State Structure allows you to:  
- create accurate MatchHistories with real matches
- create StateRegions of otherwise difficult to capture regions 
(usually due to variable imagery)
- work directly with the client's environment
- get feedback on the accuracy and efficacy of your StateImageObjects 
before running the app live
- save lots of time and avoid errors by not having to type out the State structure

