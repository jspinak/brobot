---
sidebar_position: 4
---

# Transitions with the BrobotApp 

## Problem: Defining GUI Processes Visually

When using the web-based frontend app to build and edit the state structure, we need to 
build not only states but also transitions. Since transitions are methods, this requires 
defining the transition logic visually. The transitions also need to be stored as objects in 
the project's database.  

There are a variety of ways to accomplish this. The two main options I considered were 
using a DSL and serialization. DSLs are impractical for the expressing the full power of Java. 
Serialization would allow for methods to be stored as code but introduces additional complexity. 

### Serialization

Serialization involves converting complex object graphs, which may include nested objects, circular 
references, and various data types, into a format that can be stored or transmitted. When the structure 
of the objects changes over time, maintaining backward and forward compatibility can be challenging. 

### Domain Specific Languages

A Domain-Specific Language (DSL) is designed to be more intuitive and tailored to a specific problem domain.
DSLs provide a higher level of abstraction, focusing on the specific tasks and concepts relevant to the 
domain. This reduces the complexity by hiding the underlying implementation details. DSLs are often designed 
to be human-readable and closer to natural language, making them easier to understand and use. Changes in 
the domain logic can be made more easily and with less risk of introducing errors, as the DSL abstracts 
away much of the complexity. 

I chose to use a DSL. To adapt a DSL to the Brobot framework, I decided to restrict the transition logic
to collections of GUI actions without Java code. This fits with the concept of "processes as objects" espoused
by the project and in general by model-based GUI automation. While limiting somewhat the expressiveness of 
transitions, it simplifies the creation and maintenance of Transition and StateTransitions classes. Also, the extra
flexibility provided by Java code is not entirely necessary in state transitions. The functionality 
provided by programming constructs like if-else statements can be achieved with an appropriate state structure. 
For example, a transition to the Island state in the game DoT may require recognizing the name of a 
valid island type, which can be expressed as a boolean. A failed transition would cause the framework
to search for a different path and execute a different series of transitions, the equivalent of the 
else clause in an if-else statement. In most cases, building the state structure without complex
programming logic is preferred. 


