---
sidebar_position: 2
---

# Database

## Why Use a Database?

Brobot until version 1.0.6 does not use a database. The state structure is either programmed
manually or code is produced by the state structure builder. Producing code with the state 
structure builder automates a large part of the process, but still requires manually cutting out
images from screenshots and naming them appropriately. This process can be time intensive and 
the subjectivity of state definitions can make it more complex and confusing. 

Optimally, Brobot would be able to create a state structure without any preparation from the 
user. Complete automation would require Brobot to identify states, cut out and name images, and 
save the newly created state structure. Saving the state structure in a database instead of writing
it in code has a few key advantages:
1. It adapts more easily to changes to the Brobot library. Using JavaPoet to write the state structure 
in code requires a lot of work for even small changes to the underlying library classes. 
2. The user should to be able to view the state structure. This is best done by displaying the states and
their elements visually, and this can be accomplished with a database, controller classes, and a 
React front-end. 
3. Modifying the automatically generated state structure is easier with a front-end GUI tool. The 
React front-end can provide functions to modify elements of the state structure, and changes can be 
saved directly to the database.

## Database Choice

I initially chose JPA and created Data Transfer Objects for a relational database. Since I already knew
JPA from my bachelor's courses, it seemed like the obvious choice. The data model is fairly complex 
and preparing the data with the correct annotations was not a simple task, but after all data
configuration issues were resolved, JPA worked without issues.

An LLM suggested I use a non-relational database since the data model (the state structure) is
created dynamically at runtime. I considered MongoDB since it's well integrated with Spring Boot through
Spring Data MongoDB and also has a large community. 

One of the benefits of non-relational databases is dynamic schema creation. However, since the relationship 
between elements in the state structure is clearly defined, I was unsure how Brobot would benefit from 
the flexibility of dynamic schema design.

MongoDB stores data as JSON, which is also the format I use to communicate with the React front-end 
when using JPA. Initial development with MongoDB may have been simpler than with JPA for this reason.
WIth JPA, I needed to create DTO and Response classes, for transferring data to the database and 
React front-end, for many data classes. However, my JPA solution is working and will require additional
work only if I add or modify data classes. JPA may also be more efficient; for example, the Image DTO
class stores a BufferedImage as a byte array and not a JSON object.

## Implementation Details

When states were defined with code, the state name was a enum. I changed this to a string since
names would be set during runtime. 

Many data types extended SikuliX data types. I removed this relationship to SikuliX classes since 
SikuliX classes are not serializable and caused issues with data transfer to the database and front-end.
Initializing certain data types requires reading .png files from the file system, which was previously
handled by SikuliX. Doing this without SikuliX added a dependency to javacv-platform, which uses native 
functions to read image files and convert them to BufferedImage or Mat types. 

## Multiple Projects

The Brobot library is built to work with a single GUI automation project, but the BrobotApp 
should allow for creating different state structures for different projects. BrobotApp should
store these state structures in a database for use with a client application. There are two 
ways I can approach having multiple projects:
1. Load the project's states and associated Java objects to memory before working with it.
2. Add a project ID to all data types and new service methods that return project-specific objects.

The second option introduces more code complexity but provides benefits for scalability and 
future project growth. Small state structures are practical to load into memory, but having very large
state structures in memory could cause performance issues. Concurrent access is more difficult
if the entire state model is in memory, in case at some point a Brobot automation application
would be used by multiple users simultaneously. Also, using project IDs allows for working with 
multiple projects together, which may be a desirable functionality. For example, smaller portions
of a larger project could be developed as subprojects, and when they are working the overall 
project could be activated. 
