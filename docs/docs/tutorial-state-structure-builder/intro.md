---
sidebar_position: 1
---

# Intro

This tutorial should take about **20 minutes**.  

Brobot builds the framework for your automation application by writing Java code for you. This framework
is referred to as the _State Structure_. Once you have a State Structure, writing your application
should be easy!  

These are the steps we will follow in this tutorial:  

1. Setup, easily my least favorite part of any project. Hopefully, it will run smoothly, but if it doesn't, 
   we'll get it working. Send me a message to jspinak@hotmail.com if you get stuck.    
   ![setup](../../static/img/state_structure_tutorial/frust.jpg)  
2. Let Brobot capture screenshots while we perform tasks manually  
   ![playing](../../static/img/state_structure_tutorial/playing.jpg)  
3. We'll select the screenshots we want to keep. These screenshots represent the environment that we want
   to automate.  
   ![screenshots](../../static/img/state_structure_tutorial/screenshots.jpg)  
4. Then we'll crop images from the screenshots that we want to use in our automation application.  
   ![crop](../../static/img/state_structure_tutorial/crop.jpg)  
5. We'll use Brobot to analyze the quality of our selected images and regions.  
   ![robot](../../static/img/state_structure_tutorial/robot4sm.jpg)    
6. Brobot will use the images and regions to build the State Structure for our application.  
   ![build](../../static/img/state_structure_tutorial/robot5sm.jpg)  
7. Now you have your State Structure! You can use it to write your automation application. In the last section are some
   suggestions on how to do that.  
   ![happy](../../static/img/state_structure_tutorial/happy.jpg)  

# Requirements

Any software that can cut and save images.  
  
Some suggestions:      
Mac: Preview   
Windows: Photos, Paint   
Linux: XPaint, Pinta   

# Some other stuff

This demo is meant as a guide to Brobot's State Structure build tool, as well 
as an introduction to its image analysis capabilities and how to use them to 
create a more robust application. The tutorial is done using the game 
Black Desert Online as the environment because it is a pretty, complex, and fun
environment that provides clear use cases for Brobot's functionality. You won't be able 
to automate gameplay because Black Desert has an anti-cheat. Brobot is an automation framework 
and not a cheat engine, and has no capabilities to bypass anti-cheat software. 