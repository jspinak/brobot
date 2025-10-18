---
sidebar_position: 1
---

# Intro

:::warning Game No Longer Available
Dawn of Titans was shut down in 2022 and is no longer playable. You won't be able to run this tutorial with live automation. However, this tutorial remains valuable for learning about Brobot's mocking capabilities and model-based GUI automation concepts. The mock execution will still work perfectly. For a tutorial with live automation, check out the [mrdoob tutorial](../tutorial-mrdoob/setup).
:::

:::info Version Note
This tutorial was originally created for Brobot 1.0.1 but has been updated for version 1.1.0. The original code examples are available in documentation versions 1.0.6 and 1.0.7.
:::


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

For capturing pattern images, we recommend using:
- **Windows**: Windows Snipping Tool (Win+Shift+S) - produces the cleanest patterns with best match rates
- **macOS**: Built-in screenshot tool (Cmd+Shift+4)  
- **Linux**: GNOME Screenshot or Spectacle

While the [Sikuli](http://sikulix.com/) IDE can be used to test similarity scores,
the Windows Snipping Tool consistently produces cleaner patterns that achieve better
match rates (95-100% vs 70-80%) during runtime automation.  

It is not recommended to call Sikuli functions directly when using Brobot. Doing so
will bypass Brobot's mocking capabilities and permit your application to execute
real commands during a mock run.  

This demo is meant as an introduction to the Brobot library 
and as a demonstration of its mocking functionality. It can be run
in any Java IDE on Windows, Mac, or Linux with mocking enabled. 
You can see the real execution in the 
[live automation video](./live-automation). 
