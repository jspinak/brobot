---
sidebar_position: 1
---

# Intro

This tutorial should take about **20 minutes**.  

We're going to develop a small Brobot application that will:  
- showcase basic state-based automation
- automate the creation of a labeled data set  

We will control a mobile game in an emulator, and capture regional screenshots at 
specific times.
Brobot will know where it is in the game, which will allow it to label the data correctly. 
In the end we will 
have a small collection of labeled images that we can use to train and test our 
machine learning models.

The complete code can be found in the GitHub repository 
[brobot-demo-labeled-data-DOTislands](https://github.com/jspinak/brobot-demo-labeled-data-DOTislands).  

## Requirements

The [Sikuli](http://sikulix.com/) IDE is a great tool for capturing images on your screen. Using the IDE,
you can toggle an image's similarity score to see how it affects the matches found.
This will give you a better feeling for the minimum similarity to use in different 
situations.  

It is not recommended to call Sikuli functions directly when using Brobot. Doing so
will bypass Brobot's mocking capabilities and permit your application to execute
real commands during a mock run.  

This demo is meant as an introduction to the Brobot library 
and as a demonstration of its mocking functionality. It can be run
in any Java IDE on Windows, Mac, or Linux with mocking enabled. 
You can see the real execution in the 
[live automation video](/docs/tutorial-basics/live-automation).
Update: Dawn of Titans has been shut down since 2022, but you can still learn about 
mocking and model-based GUI automation with this tutorial. If you are interested in a tutorial 
that allows for live automation, check out the [mrdoob tutorial](/docs/tutorial-mrdoob/setup). 
