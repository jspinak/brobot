---
sidebar_position: 4
---

# Action Recording

Action recording takes care of a weakness inherent in unit testing. Unit testing requires 
an exact calculation of the expected results, which can be very time-consuming.
Imagine having a Find.ALL operation that returns 20 results. Finding the correct 
locations of all 20 matches can take a while, and doing it for an entire series of 
operations can take too long for it to be considered a productive activity. Action 
recording, on the other hand, requires no preparation and gives a visual representation of 
action results. Matches, mouse movement, clicks, and drags are all illustrated on screenshots
taken when the actions were performed. These illustrated screenshots can then be scanned
to see if the actions performed as expected.  

Illustrated screenshots and the original screenshots (without the illustration) are 
both saved to the folder specified by `BrobotSettings.historyPath`. The default value
is `history/`, which refers to a folder called _history_ in the root project directory.
For illustrated screenshots, `BrobotSettings.historyFilename` gives the base name 
of the files to be saved. The default base name is `hist`. For original screenshots, 
the base name is given by the same variable that specifies the base name for 
screenshots saved for the State Structure builder: `BrobotSettings.screenshotFilename`.
The default is `screen`. Illustrated screenshots are saved in the format:
_historyFilename#-ACTION-objectActedOn_. Original screenshots are saved in the format:
_screenshotFilename#_. A history containing two actions might look like this (these are 
filenames in the history folder):

        hist1-CLICK-image2.png
        screen1.png
        hist0-FIND-image1.png
        screen0.png

The below image is the illustrated screenshot of a Find operation:

![illustrated find](/img/illustrated-find.png)

The following image shows the illustrated screenshot of a Move (mouse move) operation:

![illustrated move](/img/illustrated-move.png)