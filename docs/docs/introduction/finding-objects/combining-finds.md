---
sidebar_position: 2
---

# Combining Find Operations

Combining multiple find operations in the same Action can give us better results.
There are two ways to do this with Brobot: Nested Finds, and Confirmed Finds. Both
methods require multiple Find operations to be added to the ActionOptions object,
and call the Find operations in the order they were added to the ActionOptions. As an
example, when using the following ActionOptions, the Find.ALL operation would be called
first, and then the Find.COLOR operation:

        ActionOptions color = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .addFind(ActionOptions.Find.COLOR)
                .build();

Combining find methods can give us more accurate matches in scenarios where the 
form and color of an object are not unique. Take the example below, where we are looking
for the yellow bars above the kobolds (the top-left bar has blue circles on it). 
A relatively solid bar of color will correspond to other places on the screen, including 
the green and red bars above the character. On the other hand, the yellow color of 
the bars would also be found in other places, including on the character's weapon and
interspersed throughout the grass. One way to narrow down our search is to look for 
both a pattern and a color.  

## Nested Finds

Nested Finds find objects inside the matches from the previous Find operation. Given 
the example above, we would have many matches inside the four yellow bars. The 
ActionOptions in the example does not specify the diameter, so the matches can be of 
varying sizes.

The ActionOptions variable `keepLargerMatches` controls whether the Find operations
should be Nested Finds or ConfirmedFinds. The default value of `false` will execute a 
Nested Find.  

In the below example, all pattern matches from the Find.ALL operation are drawn in 
blue bounding boxes, and the color matches are drawn in pink bounding boxes. To the 
right of the scene are the contents of the color matches. As expected, all color matches 
are some variation of yellow, showing that they are taken only from the pattern matches of
yellow bars and not from the red or green bars.  

![nestedFind](/img/color/nestedFind.png)  

## Confirmed Finds

Confirmed Finds look for matches inside the matches from the first Find operation. 
All subsequent Find operations are performed on the match regions from the first operation.
If a match is found, the match region from the first Find operation will be returned. 
For a match to exist, all subsequent Find operations need to succeed within its region. 
In the example above, if a yellow pixel was found in the match region of a solid color bar,
the entire bar would be returned as a match object. The size of the match would equal
the size of the bar image on file.  

To set the Find operations to Confirmed Finds, the ActionOptions variable 
`keepLargerMatches` should be set to true.  

        ActionOptions color = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.FIND)
                .setFind(ActionOptions.Find.ALL)
                .addFind(ActionOptions.Find.COLOR)
                .keepLargerMatches(true)
                .build();

In the below example, the pattern matches from the Find.ALL operation are drawn in
blue and the color matches are drawn in pink. To the right of the scene are the contents of the 
color matches. The pattern match is selected in its original size. Only the yellow bars are selected.  

![confirmedFind](/img/color/confirmedFind.png)  