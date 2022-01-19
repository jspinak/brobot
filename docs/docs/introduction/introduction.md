---
sidebar_position: 1
---

# In a Nutshell

Brobot makes it easier to develop complex GUI automation. This is why.

# GUI Processes

First, a brief introduction of what a GUI automation app is: it is a
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
is what defines this process as stochastic, and it makes the development of
an automation app extremely complex. 