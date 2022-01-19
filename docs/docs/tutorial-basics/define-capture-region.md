---
sidebar_position: 6
---

# Define the Capture Region

We now need to define the region where we will capture the image of the island. 
The island always appears near the search button, so we use the search button's
location to define our island region.   

    @Component
    public class IslandRegion {
    
        private final Action action;
        private final World world;
        private final Island island;
    
        public IslandRegion(Action action, World world, Island island) {
            this.action = action;
            this.world = world;
            this.island = island;
        }
    
        public boolean defined() {
            if (island.getIslandRegion().defined()) return true;
            ActionOptions define = new ActionOptions.Builder()
                    .setAction(ActionOptions.Action.DEFINE)
                    .setDefineAs(ActionOptions.DefineAs.MATCH)
                    .setAddX(-50)
                    .setAddY(-250)
                    .setAbsoluteWidth(200)
                    .setAbsoluteHeight(200)
                    .build();
            ObjectCollection searchButton = new ObjectCollection.Builder()
                    .withImages(world.getSearchButton())
                    .build();
            Region reg = action.perform(define, searchButton).getDefinedRegion();
            island.getIslandRegion().setSearchRegion(reg);
            return island.getIslandRegion().defined();
        }
    
        public Region getRegion() {
            return island.getIslandRegion().getSearchRegion();
        }
    }
