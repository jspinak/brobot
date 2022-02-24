---
sidebar_position: 4
---

# Image Analysis

How carefully you select your images and define your regions 
will be key to the robustness and 
effectiveness of your automation application. Identifying 
the optimal images to use early on will save you lots of time and frustration. 
Ideally, you should analyze and optimize your images before writing any code. 
Brobot has functionality that is dedicated to pre-code image analysis. It works together
with the State Structure builder to give you an idea of which images may cause 
problems and what you may want to change.

## Common issues with images and regions 

Adjusting the minimum similarity of images is a common technique for situations
where an image is not recognized as we expect. The image may vary slightly but still 
needs to be found with high certainty, in which case the minimum similarity would
be lowered. Minimum similarity is also adjusted upwards, in scenarios where similar images 
exist on the screen, and it's important to find only the one we want. Both scenarios
involve trial-and-error programming, a technique that we want to avoid when
developing automation software. Ideally, we will choose images that don't require 
adjusting the minimum similarity at some point down the line. 

Sometimes we can't choose an image that is unique. For example, we have a 
close button like the 'X' at the top right of a window that is the same for 
all windows in the environment. In this case, it is best to 
set the SearchRegion to look for only the button we want to press.  
In addition, the close image should be defined as `shared` so that it is not 
used to find a State in the case that Brobot gets lost.   

Defining regions can be difficult during execution. If we are working with
images that don't always appear in the same location, we may want to identify the
area where they could appear and limit our search to this area. Even if 
we know in advance where this area is, identifying the exact coordinates on the 
screen is not always easy to do and the results can be imprecise. 
Finding regions during 
execution is especially difficult when the regions contain few or no static 
images.  

## Brobot's image analysis

During the process that builds the StateStructure, an analysis of each screenshot
is output to the console. This analysis contains information on GROUP_DEFINE and 
TRANSFER operations as well as images and their matches under the following conditions:
- at least one match is found
- an active attribute fails

## Using the image analysis

Errors in the attribute operations give us valuable information about our images.

### Multiple matches found

If an image should be found only once but is found multiple times in a screenshot,
we know that this is either a shared image or that it should be changed or replaced.
Shared images are sometimes necessary, but should include a defined SearchRegion 
when possible. When an image does not have an active MULTIPLE_MATCHES attribute and
appears multiple times, it is initialized with the field `.isShared(true)`. Images 
that are meant to be unique and appear once but are found multiple times are problems.
You should check out the screenshot and try to find the matches from the coordinates 
given in the image analysis. If changing or replacing the image is not possible, think
about either defining the image's SearchRegion
to exclude the other matches or using a different strategy to achieve your goal.  

### Image not found  

Not finding an image at all when it should be found is a sign of a poorly chosen image,
or of a non-static image that is not well suited for traditional image recognition. 
If there is no way to use an image that is more static, you may want to change your 
strategy for this specific process. There are almost always multiple ways to achieve 
things in process automation.  

Failure to define a region is usually caused by not finding an image. Finding 
too many matches or finding an image in the wrong place can result in an 
incorrectly defined region. The coordinates and size of defined regions are printed
to the console along with information about images and matches.  

## Defining regions  

Having well-defined SearchRegions will make your process flow more robust, 
and many regions can and should be defined before execution by the 
State Structure build process.  

### Defining regions without static images  

Static images are ones that do not change their appearance. Fixed-location images are
ones that do not change their location. Finding regions without static, fixed-location 
images is very difficult during a real run, but can be easily captured when using 
screenshots. Take, for example, the minimap portrayed here after this paragraph. 
To find the region of the minimap during a real run, I would try to find 
the one static and fixed-location image, the small circle in the top left with 
2D written on it,
and then adjust the region around this match by guessing and readjusting my guesses
through trial and error.
If there were no static, fixed-location images, I would try to find 
such images outside the minimap and adjust the region accordingly.   
<img src="https://jspinak.github.io/brobot/img/minimap.png" alt="minimap" width="200"/>  

The attribute to use in this situation is REGION. When the filename of an image
contains the String `_r`, it will search for the image and write a StateRegion
with a pre-defined SearchRegion to the corresponding State. In order not to 
receive error messages in the image analysis for every other screenshot, make 
sure to specify the screenshot to use after the `_r` (i.e. `_r28`).  

### Defining regions with variable-location images

There are regions that have a variable number of images, each with 
variable locations. We may want to find the region that includes all of them, 
in order to make our searches faster and more effective. 
There are two ways to do this. The first is to take an image of the area you 
think you need and give 
it the TRANSFER attribute for this screenshot. The TRANSFER attribute will 
transfer the match to the SearchRegions of all images in the State.  
Another option is to use GROUP_DEFINE, which will set the SearchRegions of each
image in the group to be the union of their matches.

### Defining the region of a State

Many States have one area of the screen where all the State objects can be found.
It is a good idea to set this region to be the SearchRegion for all images in the 
State. Doing this with the StateStructure builder is easy. The attributes to use 
are TRANSFER or GROUP_DEFINE. 

