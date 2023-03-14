---
sidebar_position: 1
---

# Why Brobot?

The goal of Brobot is to allow for the creation of a Visual API using both static and dynamic images.
A Visual API is a representation of the environment that can be used to control it. Similar to a
programmatic API, a Visual API can be accessed with code and manipulated with the GUI automation tools
provided by Brobot. To give an example, Starcraft made a programmatic API available to AI researchers
to test reinforcement learning techniques. What if Starcraft hadn't made an API available yet you
wanted to use the game to test your AI? Brobot attempts to solve this problem by converting visual cues
into a programmatic API.  

This is, however, not the only use for Brobot. Brobot builds a model of the underlying environment, which
allows you to test your automation application's codebase. Previous to Brobot, this was not possible,
making the development of large automation applications very difficult due to the highly stochastic nature
of automation. Think about it like this: You have a written an application that
depends on an external API in almost every single function of your program, and the output of the external
API is highly variable. How do you test your codebase in this case? Well, traditionally, you would write
a mock test for each call to the external API, simulating the variability available in its output. While
different functions of the API are called in almost every single function call in your program, the test
code would quickly outgrow the underlying program code. This is also true for testing an automation application.
Without Brobot, to test an automation app, the environment's output would need to be simulated at every step,
making the test code larger and more complex than the automation application. Brobot takes care of
this for you, thus allowing for testing without this level of complexity.

# GUI Processes

A GUI automation application is a
program that sees what a human would see and interacts with the
computer by using the mouse and keyboard. Common examples of GUI automation apps
are testing applications for other software and bots that play games autonomously.

## GUI Automation is not Tested

Testing software during development is considered good practice
because it makes the development process more efficient. Bugs can
be identified immediately and corrected before they influence other
parts of the application. As the app becomes more complex, the benefits
from testing become more profound.

GUI automation applications are not tested. This makes their development,
especially when the applications become complex, error-prone and
time-consuming. But, why aren't they tested?

## GUI Processes Are Stochastic

GUI automations apps are not tested because the processes that are being 
automated are stochastic processes. 

### Sikuli vs Selenium

To illustrate why this is important, let's compare two different pieces of automation software:
Sikuli and Selenium. Both Sikuli and Selenium can automate process flows in
Web sites, but they do it differently. Sikuli recognizes images and can click
on these images or use the scroll bar to navegate through a Web site. Selenium
accesses the html of the Web site and manipulates Web objects directly. The process
Sikuli uses is a stochastic process since it is not sure to recognize an image
correctly and attempting to drag the scroll bar is not guaranteed to actually scroll
the page. In contrast, the process Selenium uses is guaranteed to succeed since
Selenium is working with the html objects themselves and not their representations.
So why would anyone use Sikuli to automate the use of Web pages if it introduces
added complexity? In short, you shouldn't use Sikuli to automate Web pages. Sikuli
should be used only where Selenium cannot be used, which is everywhere outside
of the Web. Outside of the Web, most programs do not provide an API for
developers to manipulate the program's objects directly. The only way to automate
these programs is to interact with the program in the same way a person
would: by looking at the screen and using the mouse and keyboard.

### The Difficulties in Testing a Stochastic Process

But why aren't these applications tested during development? Well, how?
How do you test if your automation app will correctly scroll down on the page?
Traditionally this was done through trial and error. A developer would code the
app to scroll down and then run it to see if it worked. This is ok for small
applications, but when an app becomes complex this is a huge problem for
development. The longer it takes to recognize an error, the more time-consuming
and frustrating the development process will be. Also, code in a process automation app
is highly interdependent, meaning that changes in one part of the code base will
affect the functionality in other parts. Not scrolling correctly will affect the
success of the code tasked with what to do at the bottom of the page. With GUI
automation, potential errors are everywhere. Recognizing the minimize window button
by mistake could result in a minimized program window and your automation
app being unable to continue. Another program could pop up a window in front of your
target program to ask the user to confirm an update. The existence of uncertainty
is what defines this process as stochastic, and it adds a lot of complexity to 
the development of automation applications. 