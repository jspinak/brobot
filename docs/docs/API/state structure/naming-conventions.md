---
sidebar_position: 2
---

# Naming Conventions

The names you give your image files control how Brobot builds the State
Structure. There are 4 main parts to the filename:
1. State name
2. Image name
3. Image attributes
4. Transitions  

The State name is always the first String, and is directly followed by a '-' and 
then the Image name. Following the Image name are the attributes, denoted by '_',
and at the end are the transitions '~'.  

stateName-imageName_attributes~transitions  

An example of a state name is: mainMenu-menuItem_23,34_g45~homeState.  
This will code to a StateImageObject called
menuItem in the State MainMenu that should appear on screenshots 23 and 34 and
will be used with other images to define a SearchRegion for all images in the group
on screenshot45. Clicking on this image starts the Transition to the State HomeState. 