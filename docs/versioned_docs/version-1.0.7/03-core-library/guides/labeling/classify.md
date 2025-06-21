---
sidebar_position: 2
---

# The CLASSIFY Action

_Classification requires version 1.0.5 or greater._  

The CLASSIFY Action differs from Find.COLOR principally in that it finds the largest matches and not the 
matches with the best scores. The largest matches usually correspond to semantic objects and the CLASSIFY Action 
is usually concerned with finding objects. 

CLASSIFY saves two images to the _history_ folder. 
1. The scene, with bounding boxes showing the matches and the search regions, and the contents of the matches
displayed to the right of the scene in their own boxes. 
2. The scene displayed as classes. Each image is broken down into k-means color clusters, and the first of these
clusters is chosen as the _display color_ for the image. This _display color_ appears on each pixel in the scene where
the pixel belongs to this class (i.e. the pixel is most likely to belong to this image). Bounding boxes (in white) 
are drawn around clusters of pixels belonging to a target image's class as well as the search regions (light blue).
The selected clusters have bounding boxes in pink. At the right of the scene, the Brobot images are shown with the 
contents of all of their image files, as well as the colors of the k-means clusters chosen. For example, if the k-means
chosen was 3, the center colors of the 3 clusters are shown.

## Example 1: 

CLASSIFY with 1 target image, 1 background image, a k-means of 3, and 3 max matches.

    BrobotSettings.saveHistory = true;
    BrobotSettings.mock = true;
    BrobotSettings.screenshot = "kobolds1.png";

    ActionOptions findClass = new ActionOptions.Builder()
        .setAction(ActionOptions.Action.CLASSIFY)
        .setMaxMatchesToActOn(3)
        .setKmeans(3)
        .build();

    ObjectCollection target = new ObjectCollection.Builder()
            .withImages(classifyState.getKobold())
            .build();
    ObjectCollection additional = new ObjectCollection.Builder()
            .withImages(classifyState.getGrass())
            .build();

    action.perform(findClass, target, additional);

This configuration gives us the following results:

![kobolds1.png](/img/classify/kobolds1.png)  

![kobolds1.png](/img/classify/kobolds1_classes.png)  

If we change the scene to kobolds2.png, we get the following results:

![kobolds2.png](/img/classify/kobolds2.png)  

![kobolds2.png](/img/classify/kobolds2_classes.png)

## Example 2:

To build a labeled dataset for localization, only the best match per scene is needed. If the game character stays
in this position while screenshots are taken, a large dataset can be built without supervision. Other things
could be done to improve the accuracy of the matches, including specifying search regions, using more images
for classification, and placing the game character in an area with only grass.   

    ActionOptions findClass = new ActionOptions.Builder()
        .setAction(ActionOptions.Action.CLASSIFY)
        .setMaxMatchesToActOn(1)
        .setKmeans(3)
        .build();

![kobolds1b.png](/img/classify/kobolds1b.png)  

![kobolds1b.png](/img/classify/kobolds1b_classes.png)  

![kobolds2b.png](/img/classify/kobolds2b.png)  

![kobolds2b.png](/img/classify/kobolds2b_classes.png)

