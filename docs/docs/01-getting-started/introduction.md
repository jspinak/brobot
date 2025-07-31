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
of automation. Think about it like this: You have a written an application that depends on an external API in almost every single function of your program, and the output of the external API is highly variable. How do you test your codebase in this case? Well, traditionally, you would write
a mock test for each call to the external API, simulating the variability available in its output. While
different functions of the API are called in almost every single function call in your program, the test
code would quickly outgrow the underlying program code. This is also true for testing an automation application. Without Brobot, to test an automation app, the environment's output would need to be simulated at every step, making the test code larger and more complex than the automation application. Brobot takes care of this for you, thus allowing for testing without this level of complexity.

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

GUI automations apps are not tested because the processes that are being automated are stochastic processes. 

### Sikuli vs Selenium

To illustrate why this is important, let's compare two different pieces of automation software:
Sikuli and Selenium. Both Sikuli and Selenium can automate process flows in
Web sites, but they do it differently. Sikuli recognizes images and can click
on these images or use the scroll bar to navigate through a Web site. Selenium
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

For a detailed exploration of the formal models and academic principles behind Brobot, please see the [Theoretical Foundations](../05-theoretical-foundations/introduction.md) section.

# Origin of the Name Brobot

My brother lives in Seattle. I live in Düsseldorf. That's 7998 kilometers or 4970 miles,
far enough so that night overlaps only in winter. Keeping in touch requires extra 
motivation, something like...like, having the same parents? No, something less meaningful...
like a witch's brew of competition, strategy, and entertainment. We used to 
play computer games together as kids, battling it out with my troops against his, or 
working together in weekend slumber parties in his room to defeat monsters in dungeons.  

We chose a game to fit our lifestyles: his hectic lifestyle, dominated by performing
reconstructive retina surgery and pondering the great philosophical dilemma of when 
a kid was too grown and too aware to be read the book 
[Go The F**k to Sleep](https://www.youtube.com/watch?v=teIbh8hFQos&t=192s), and my 
hectic lifestyle, the result of deliberate overindulgence in intellectual challenge 
and adventure,
studying full-time for a computer science degree in my then only fifth-best language, 
an activity that was both perpetrator and rehab for a condition known informally as 
post-4-year-world-travel dopamine withdrawal.  

We picked a game that we could play given our constraints, and mobile it was. As is
typical for mobile games, it evolved pretty quickly into 95% grind and 5% strategy. 
I have a limited tolerance for grind in all of its forms. I wanted us to be able to
discuss strategy together. Playing the game, after all, was about the two of us 
connecting and spending time together. At the same time, it would be more fun if we
didn't completely suck at the game. At this point I was in the second year of my degree,
and the theoretical classes had graciously made space for a few practical classes 
focused on programming, dependency injection, and other goodies such as 
object calisthenics. I needed a personal project to practice what I was learning. I 
needed a sidekick to help me up the learning curve, an 
[Igor to my Frankenstein](https://www.youtube.com/watch?v=nxxSIX3fmmo), 
a [Piglet for my Pooh](https://www.youtube.com/watch?v=-cCGuL0-sJw), 
a [Donkey to my Shrek](https://www.youtube.com/watch?v=6Q6qHRHTTPg), 
a [Hobbes to my Calvin](https://cdn.vox-cdn.com/thumbor/0qgaVMD7Kve6W5yTyrd0LQZ1qpM=/0x0:3500x2425/1920x0/filters:focal(0x0:3500x2425):format(webp):no_upscale()/cdn.vox-cdn.com/uploads/chorus_asset/file/19964119/calvin_hobbes_final_comic_strip.jpg), 
a [Bender for my Fry](https://www.youtube.com/watch?v=Hj7LwZqTflc). 
I soon started calling my sidekick Brobot, and the name stuck.  

The game didn't work on an emulator, so I ran it initially on an old phone connected 
to my computer by remote control software. As you can imagine, this setup was a real 
headache, but it did give me a crash course on the stochasticity inherent in 
process automation. There was an intellectual challenge just out of reach of my 
abilities, and I was hooked. My brother and I eventually gave up on the game, but 
Brobot hung on, motivated by universal interests shared by humans and robots alike, 
things such as state-based automation and the testing of stochastic processes. 
Everything seemed to move in unison and with purpose, until one day...   

It was too sexy. It walked into the room with a neural mesh net skirt, 
and lots and lots of layers. I mean, baby got backpropagation. 
It had so many layers that you wondered if it was even comfortable doing a 
simple forward pass. I tried to talk with Brobot and explain that this ANN, as 
Brobot called its new obsession, is not as practical as Brobot's current friends. ANN
won't help it complete its tasks, ANN might introduce more 
uncertainty to processes and Brobot has been working to reduce uncertainty. 
Brobot listened carefully, or ignored me intentionally while giving signals of listening
(it's hard to tell with robots, sometimes they seem like black boxes inside). Brobot
turned to me and said, in a voice that you would imagine coming from a retro-looking yet 
emotionally evolved and empathetic robot: "I think ANN can be a good friend, just
give me some time".   

To be continued...