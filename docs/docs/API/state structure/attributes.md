---
sidebar_position: 3
---

# Attributes

An attribute describes how an image should behave on a certain page. Attributes
can be page specific (i.e. `_m2,3`) or affect all screenshots (`_m`). All Attributes
are listed below with details on what they do and how they influence the State Structure. 

## APPEARS
Modifier: `_`  
The associated image should appear in the screenshots that are listed after the modifier. 
For example, an image that should appear on screenshots 2 and 3 would have `_2,3` in 
the filename. An image that should appear in every screenshot will have just `_` in the 
filename without any screen numbers. 

## APPEARS_EXCLUSIVELY  
Modifier: `_e`  
The image appears exclusively in the screenshots listed after the modifier. For 
example, if we have 10 screenshots labeled screen0, screen1, ..., screen9, and 
have the String `_e2,4,5` in our filename, the image will be expected to appear in
screenshots 2, 4, & 5, but not in the remaining 7 screenshots (0,1,3,6,7,8,9). 
The modifier `_e` by itself has the same effect as the modifier `_` by itself.  

## DOESNT_APPEAR
Modifier: `_x`   
The image doesn't appear in the screenshots listed after the modifier. 

## MULTIPLE_MATCHES  
Modifier: `_m`  
Allows for, but does not require, multiple matches. 

## SINGLE_MATCH  
Modifier: (none)  
Since it is more common for an image to require a single match than multiple matches, 
this attribute is not written in the filename but deduced from the absence of the 
MULTIPLE_MATCHES attribute.  

## VARIABLE_LOCATION  
Modifier: `_v`  
This attribute allows the image to vary its location. The StateImageObject written
to the State Structure for this image will
use an Image object (which allows for variable locations) instead of a RegionImagePairs 
object (which allows only for a set of fixed locations). The image analysis performed 
by the State Structure builder will not flag images that return multiple matches 
when this attribute is active.  

## FIXED_LOCATION  
Modifier: (none)  
Most State objects are expected to exist in the same location, and since this is
the default it is not included in the filename. Filenames that do not include a
VARIABLE_LOCATION attribute will have FIXED_LOCATION for every screenshot.  

## DEFINE  
Modifier: `_d`  
Using this attribute we can define the SearchRegion of an image as the region of
its match. The SearchRegion will be defined only once. If screenshot numbers are listed 
after the modifier, Brobot will only use these screenshots to define the image; 
otherwise, it can be defined on any screenshot. DEFINE is used for images with
fixed locations.  

## GROUP_DEFINE  
Modifier: `_g`  
Multiple images in a State can be used to define a SearchRegion for each of them
that includes all the images. You may want to do this for a set of images that 
appear in a certain region, but not in fixed locations. Brobot will continue to 
define group regions for screenshots where this attribute is active, and
will replace the SearchRegion of the images when the newly defined region is 
larger than their current SearchRegion.  

## REGION  
Modifier: `_r`  
When this attribute is active, the first match found will be stored in the 
image's SearchRegion. When the Java classes are written, this SearchRegion 
will be used to create a StateRegion defined by the SearchRegion and no 
StateImageObject will be created.  

## TRANSFER
Modifier: `_t`  
Similar to REGION, this attribute does not allow the image to get written to the 
State Structure as a StateImageObject.
Unlike REGION, it also does not allow the image's SearchRegion to become a StateRegion. 
Instead, the image's 
match is copied to the SearchRegions of all images in the State. 
These SearchRegions are updated as long as the target image's SearchRegion is 
either not yet
defined or smaller than the new Region. This is another approach, along with 
GROUP_DEFINE, to determine the SearchRegion for variable-location images.  