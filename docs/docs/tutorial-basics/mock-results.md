---
sidebar_position: 9
---

# Mock Results

## Console Output

    + add WORLD to active states
    Open State ISLAND
    Find path: [WORLD] -> ISLAND
    _(score)_Paths Found_
    (2)-> WORLD -> ISLAND
    |CLICK WORLD.searchButton| Find.FIRST 36:05 found=true <click> wait-0,2 <click> CLICK ✓
    states to activate: [ISLAND]
    |FIND ISLAND.island type text| Find.FIRST 36:05 found=true + add ISLAND to active states FIND ✓
    ✓ Transition WORLD->ISLAND successful.
    Active States: [WORLD, ISLAND]

    |CLICK WORLD.searchButton| Find.FIRST 36:05 found=true <click> wait-0,2 <click> CLICK ✓
    |GET_TEXT wait-3,0 Find.FIRST GET_TEXT ✓
    text = Mines
    |DEFINE Define as: MATCH| DEFINE ✓
    Save file with base path DOTislands/labeledImages/Mines
    
    |CLICK WORLD.searchButton| Find.FIRST 36:09 found=true <click> wait-0,2 <click> CLICK ✓
    |GET_TEXT wait-3,0 Find.FIRST GET_TEXT ✓
    text = Farms
    Save file with base path DOTislands/labeledImages/Farms
    
    |CLICK WORLD.searchButton| Find.FIRST 36:12 found=true <click> wait-0,2 <click> CLICK ✓
    |GET_TEXT wait-3,0 Find.FIRST GET_TEXT ✓
    text = Lakes
    Save file with base path DOTislands/labeledImages/Lakes

... <i>this repeats 100 times (if you entered 100 as the max images to save)</i>  

## Playing with the Mock

You can now play around with the code to see what mock results you get. 
As opposed to writing an automation application without mocking capabilities,
you don't have to wait for real execution to see the effects of any changes. Go 
ahead and make the following changes:  
1. Comment out StateTransitions, for example IslandTransitions by 
commenting out the line in the IslandTransitions class that adds 
its StateTransitions to the repository:  
   //stateTransitionsRepository.add(transitions);  
Run the code again to see the results. Notice how you get immediate feedback
that there is a mistake and know that you forgot to include a Transition. 
Without mocking, you would have to wait until this transition occurs to get
an error. Depending on the size and complexity of your application, this could
take a long time. With mocking it happens immediately, similar to normal 
integration testing. 
2. Change the name of one of the image files. See the errors produced in the mock. 
3. Add text to the GetText snapshot. Add text that you might expect to see, 
such as "Mnes" and "Frms". As expected, the demo will not recognize these 
inputs as being real island types. This is a way to use mocking to refine
your algorithm. While not technically an error, the application should 
probably classify "Mnes" as Mines and "Frms" as Farms. Having these values
in the snapshot allows you to refine and test your code efficiently, without
having the long cycles characterized by real runs. 



