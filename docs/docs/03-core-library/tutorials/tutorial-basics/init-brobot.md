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

This needs just one line to specify the folder with images to reference,
and one line to turn on mocking.

        // setup brobot
        ImagePath.setBundlePath("images.sikuli");
        BrobotSettings.mock = true;

## Set the Active States

The InitialStates class allows you to specify various groups of states
that could possibly exist when your application starts. This is practical
if the program(s) you are controlling have different starting configurations.
InitialStates can also be used for a more targeted search if Brobot gets lost,
instead of searching for all states with the StateFinder.

We allow for two possible single-state starting points: HOME and WORLD. These
states don't exist yet, but we will add them soon. The first parameter in
addStateSet gives the probability of these states appearing as the starting point.

        // find initial active States
        InitialStates initialStates = context.getBean(InitialStates.class);
        initialStates.addStateSet(90, WORLD);
        initialStates.addStateSet(10, HOME);
        initialStates.findIntialStates();


