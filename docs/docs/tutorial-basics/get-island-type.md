---
sidebar_position: 7
---

# Get Island Type

Here we determine the island type from the on-screen text.

## Go to a new Island and Get its Type

Here we call the transition directly instead of using Brobot's state management system
by calling StateTransitionsManagement.openState(ISLAND). Normally we would use the
state management system, which takes care of everything related to transitions, including
finding and traversing paths. In this case, the state management system would not do
anything because the Island state is already active. In this special situation, we want to
perform the transition activities from the World state to the Island state even though the
Island state is already present. The reason we want to do this is that this transition will
take us to a different island. The Island state does not know whether the specific island
has changed. It only recognizes that it is on an island.

If the text we found on-screen contains any of these substrings,
we return the corresponding island type. Otherwise, we return an
empty string.

    public String getIsland() {
        worldTransitions.goToIsland();
        String textRead = getIslandType();
        for (Map.Entry<String, String> type : islandTypes.entrySet()) {
            if (textRead.contains(type.getKey())) return type.getValue();
        }
        return "";
    }

## Capturing Text

With the following action we retrieve the text found where the island
names are. We know that at least one name was already found when
the state management system opened the Island state, since the IslandName
variable is the only StateImageObject that identifies the Island state.
Since the IslandName has already been found once, its SearchRegion will be defined
as the region where it was first found. We can then use this SearchRegion to retrieve
the name of the new island type.

    private String getIslandType() {
        ActionOptions getText = new ActionOptions.Builder()
                .setAction(ActionOptions.Action.GET_TEXT)
                .getTextUntil(ActionOptions.GetTextUntil.TEXT_APPEARS)
                .setUseDefinedRegion(true)
                .setMaxWait(3)
                .setPauseBeforeBegin(3)
                .build();
        ObjectCollection islandName = new ObjectCollection.Builder()
                .withRegions(island.getIslandName().getSearchRegion())
                .build();
        return action.perform(getText, islandName).getSelectedText();
    }

## Converting Text to Island Types

First, we create a variable that maps pieces of the island type
words that are representative of these words. For example, "Mine"
is representative of the type Mines; if our text retrieval function
finds the word "Minez", we can assume that the real text is "Mines",
since the string "Mine" is part of "Minez".

    private Map<String, String> islandTypes = new HashMap<>();
    {
        islandTypes.put("Burg", "Castle");
        islandTypes.put("Mine", "Mines");
        islandTypes.put("Farm", "Farms");
        islandTypes.put("Moun", "Mountains");
        islandTypes.put("Fore", "Forest");
        islandTypes.put("Lake", "Lakes");
    }

