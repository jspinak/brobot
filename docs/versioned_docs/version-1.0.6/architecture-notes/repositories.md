---
sidebar_position: 3
---

# BrobotApp

## Issues with a Single Project Repository

I initially added the database and web functionality to the Brobot
repository on GitHub. The React front-end code went in the root folder
'web', added to the set of root folders containing 'library' and 'docs'.

Problems with using MVC in a Spring Boot library caused me to consider
creating a new repository for these functions. The first problem was that
the GET requests were not visible in the http endpoints. Additionally, a client
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
repository holds the software necessary to create state models using a GUI. I'll refer to this software as BrobotApp 
or the App and continue referring to the library as Brobot. 

The React front-end lives in a separate repository, under /web. I'll call it BrobotWeb or Web. It is to be used
with the App to facilitate creating state models.

I created an additional folder for the logging capabilities. It is best practices not to have REST communication
in a library module. For this reason as well as to have more modularity and separation of concerns, logging and 
communication with REST endpoints for the Elasticsearch containers are now handled by the logging module. The 
folder is /log and I call it BrobotLogging or Logging. 

# Multi-Module Project

I decided to compartmentalize functionality with a multi-module project and keep all modules in the same Github
repository.
The modules are:
- library
- app
- log
- web
- library-test

The library tests are in a separate module because they require running a Spring Boot application and use the 
annotation @SpringBootTest, but the library module is not a Spring Boot Application and does not have the 
@SpringBootApplication annotation. Keeping the tests in a separate module allows for separation of concerns and
keeps the library's dependencies specific to its functionality. Make sure to exclude the module library-test from 
the production jar. 


