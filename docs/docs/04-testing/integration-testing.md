---
sidebar_position: 2
---

# Integration Testing

Integration testing is performed by simulating the environment the application runs in 
and the results of actions taken on objects in this environment. The model of the environment
is represented by a sample distribution of action-results pairs for state objects, as well
as probabilities that determine the success or failure of state transitions when the
application is run as a test. Running the app as a test is typically referred to as 
mocking. Mocking is the process of simulating actions instead of executing them in a real run. 
An integration test in Brobot runs the entire application in a mock run, and is 
done by simply setting the variable `BrobotSettings.mock` to true.  

## Mocking

Mocking is used for application testing when making calls to a third-party API that the
developer cannot control. For example, calls to an external database can be mocked in the
test methods in order to simulate real data. Mocking in brobot is similar to this idea.
Brobot uses Sikuli for interacting with the GUI and wraps every Sikuli method that it
calls to be able to produce a mock instead of a real action. A Brobot application can then
be mocked to produce text outputs and is run exactly as it would otherwise. The higher level
code in a brobot application does not know if it is running live or in a mock. The mocks
happen only at the wrapper level and the wrappers return the same outputs as they would
during a real run (plus some additional logging when enabled). Stochastisticity is modeled
by sampling from the potential outcomes saved with state objects, and from the
probabilities given to the success or failure of state transitions. 

![wrappers](/img/wrappers.jpeg)

Mocking can uncover errors in the code in the same way that traditional testing, for
example JUnit testing, does. You don't have to wait 30 minutes to realize that you
forgot to name the new image you saved, and your application can't find it. This
happens instantly.

Mocking also provides insight into how robust your code is. Parts of the code with
narrow paths (little redundancy in making a transition from state A to state B)
may perform poorly if a state has a low probability of appearing. You can introduce
process flow errors into the mocks, including sending your process to an unknown state,
to see how your app will behave.

## Match Histories

Adding the code to guide the mock involves initializing state objects either with
a history of action results (matches produced by different actions on this object)
or with probabilities. It is recommended to initialize
with action results since they typically lead to more realistic mock runs. When
using the state structure builder, states are built by Brobot using match histories.
The state structure builder does the following:
1. saves screenshots of the target environment
2. analyzes all selected images on these screenshots
3. writes Java code to build the state structure, and initializes state objects with match histories taken from the image analysis

The below example was created by the state structure builder. It initializes the
image with a history of Find.ALL operations that were performed during image analysis
on screenshots of the target environment.

![StateImageObject](/img/StateImageObject.png)

## Console Output

Detailed process output allows the user to follow the process flow and find 
the sources of potential errors. All the output provided by real runs, 
included java console output, are also provided by mock runs. Discovering 
a hidden null pointer exception can take place almost immediately as opposed to 
hours into a real run.

![console output](/img/mock-output.png)

## Assertions

Brobot has testing capabilities that allow the programmer to compare expected
results to the results obtained in a mock run, giving color coded feedback on
success or failure of the mock run.

![test output](/img/test-output.png)



