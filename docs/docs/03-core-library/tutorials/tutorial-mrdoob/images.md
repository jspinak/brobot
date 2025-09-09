---
sidebar_position: 3
---

# State Images

:::info Version Note
This tutorial was originally created for an earlier version of Brobot but has been updated for version 1.1.0. The original code examples are available in documentation versions 1.0.6 and 1.0.7.
:::

There's one more step before starting coding. Let's capture the pattern images we need. These images 
will define our states and be used to move from state to state. Sometimes, an image will be used
for both identifying a state and for movement between states. 

**Important**: For best pattern matching results, use your OS's native screenshot tool:
- **Windows**: Windows Snipping Tool (Win+Shift+S) - achieves 95-100% match rates
- **macOS**: Built-in screenshot (Cmd+Shift+4)
- **Linux**: GNOME Screenshot or Spectacle

We want 3 PNG images: the harmony icon, the about link, and the about text. Create a folder "images" 
in the root directory and save these images there with the names harmonyIcon.png, aboutButton.png, 
and aboutText.png.  

![Images](/img/mrdoob/mrdoob_images.png)

![Images in Folder](/img/mrdoob/mrdoob_filestructure_images.png)

