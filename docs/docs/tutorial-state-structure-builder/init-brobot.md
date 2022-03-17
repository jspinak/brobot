---
sidebar_position: 3
---

# Initialize Brobot's Environment

## Configure Spring to Automate the GUI

In the executable class, we need to add the following lines for
Brobot to be able to execute real runs:

        SpringApplicationBuilder builder = new SpringApplicationBuilder(Run.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);

These lines should replace any other code involving SpringApplicationBuilder and
the ConfigurableApplicationContext.

## Setup Brobot

This needs just two lines to specify the folder with images to reference and
the package name. The package name written here is the package name of the 
tutorial in YouTube; you should use the package name of your project.   

Make sure mocking is turned off. It is by default turned off, but it 
doesn't hurt to specify it explicitly. We want to capture screenshots and run real
find operations on the saved screenshots. It's also convenient to have 
mocking set here for testing your application later.  

        // setup brobot
        ImagePath.setBundlePath("images");
        BrobotSettings.packageName = "com.example.bdodemo";
        BrobotSettings.mock = false;



