---
sidebar_position: 3
---

# BrobotApp

## Issues with a Single Project Repository

I initially added the database and web functionality to the Brobot
repository on GItHub. The React front-end code went in the root folder
'web', added to the set of root folders containing 'library' and 'docs'.

Problems with using MVC in a Spring Boot library caused me to consider
creating a new repository for these functions. The first problem was that
the Get requests were not visible in the http endpoints. Additionally, a client
Brobot application would not stay active for REST communication, despite its 
dependency to the Brobot library, which was configured as a Spring MVC project
with @Service and @Controller classes.

## Solutions

Since Brobot is a Spring Boot library, it cannot be run independently as a Spring
Boot applications. This is necessary for it to be included as a dependency in Spring
Boot client applications and for its beans to be available to the client. Libraries do 
not typically include MVC functionality and I presume this is the cause of the issues I 
had with the MVC functionality. 

The solution is to create a separate repository for a Spring Boot application (and not a library)
that includes the @Service and @Controller classes and has Brobot as a dependency. This 
repository holds the software necessary to create state models using a GUI. I'm also transferring
the React front-end to this repository since the front-end is part of this functionality.
I'll refer to this software as BrobotApp and continue referring to the library as Brobot.
