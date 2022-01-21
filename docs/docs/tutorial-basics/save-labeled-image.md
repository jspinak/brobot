---
sidebar_position: 8
---

# Save Labeled Images

## The SaveLabeledImages Class

In the main program loop, we first go to the Island state. If this 
fails we exit the program, to keep things simple. Normally you would 
want Brobot to find out where it is and either make its way back to the 
target state or do something else.  

We then loop for the number of images we wish to save, get a new island 
type, and save it to file. 
    
    @Component
    public class SaveLabeledImages {
    
        private StateTransitionsManagement stateTransitionsManagement;
        private ImageUtils imageUtils;
        private GetNewIsland getNewIsland;
        private IslandRegion islandRegion;
    
        public SaveLabeledImages(StateTransitionsManagement stateTransitionsManagement,
                                 ImageUtils imageUtils, GetNewIsland getNewIsland,
                                 IslandRegion islandRegion) {
            this.stateTransitionsManagement = stateTransitionsManagement;
            this.imageUtils = imageUtils;
            this.getNewIsland = getNewIsland;
            this.islandRegion = islandRegion;
        }
    
        public void saveImages(int maxImages) {
            String directory = "labeledImages/";
            if (!stateTransitionsManagement.openState(ISLAND)) return;
            for (int i=0; i<maxImages; i++) {
                String newIslandType = getNewIsland.getIsland();
                Report.println("text = "+newIslandType);
                if (!newIslandType.isEmpty() && islandRegion.defined()) {
                    imageUtils.saveRegionToFile(islandRegion.getRegion(), directory + newIslandType);
                }
                Report.println();
            }
        }
    }

## Run 'saveImages' from the Executable Class

After the code to initialize the beginning active states, add the following code.
This will save a maximum of 100 images.

    // get and save labeled images
    SaveLabeledImages saveLabeledImages = context.getBean(SaveLabeledImages.class);
    saveLabeledImages.saveImages(100);

That's it! Now your demo application should be ready to go. Run it to see the 
mock output in your console. 