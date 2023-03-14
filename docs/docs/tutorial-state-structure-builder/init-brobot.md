---
sidebar_position: 3
---

# Initialize Brobot's Environment

## Configure Spring to Automate the GUI

In the executable class, we need to add the following lines for
Brobot to be able to execute real runs:

        SpringApplicationBuilder builder = new SpringApplicationBuilder(TheNameOfThisClass.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);

These lines should replace any other code involving SpringApplicationBuilder and
the ConfigurableApplicationContext. Replace TheNameOfThisClass with the name of
the class.

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

## Sample Application Class Code

Your code should now look similar to this:

        package com.example.bdodemo;

        import org.springframework.boot.autoconfigure.SpringBootApplication;
        import org.springframework.boot.builder.SpringApplicationBuilder;
        import org.springframework.context.ConfigurableApplicationContext;
        import org.sikuli.script.ImagePath;
        import io.github.jspinak.brobot.BrobotSettings;

        @SpringBootApplication
        public class BdodemoApplication {

            public static void main(String[] args) {
                SpringApplicationBuilder builder = new SpringApplicationBuilder(BdodemoApplication.class);
                builder.headless(false);
                ConfigurableApplicationContext context = builder.run(args);

                // setup brobot
                ImagePath.setBundlePath("images");
                BrobotSettings.packageName = "com.example.bdodemo";
                BrobotSettings.mock = false;
            }
        }

