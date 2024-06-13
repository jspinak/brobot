---
sidebar_position: 1
---

# Tutorial for Using the Latest Brobot Code in Github

This tutorial is for setting up a project to use the latest Brobot code in Github. The latest code has not 
been packaged and uploaded to Maven Central yet, but can still be used if you want the cutting edge features currently 
in development.

These are the steps we will follow in this tutorial:  

1. Create a folder for both your app and the Brobot library. Both repositories will live here.
2. There is a demo app called myApp in the myBrobotApp repository. It can be found at
   https://github.com/jspinak/myBrobotApp. Fork the repository and then clone your forked 
   repository to your local machine. Put it in the folder you created. You can rename it if you like.
2. Clone the Brobot repository and place it in the folder you created, next to your app.
3. Create a JAR file of Brobot's library module. The following image shows how to do this in IntelliJ.
   To work with the latest code, you can update the Brobot repository and then recreate the JAR file.
   ![create a JAR with Gradle](../../static/img/tutorial_latest_code/make_jar_with_gradle.png)
4. Your app's build.gradle file should have this JAR file as a dependency. You also may have to add the library manually.
   in IntelliJ: Project Structure -> Project Settings -> Libraries -> + -> find the location of the JAR file. It should
   be in the location in the image below. If the relative filepath doesn't work, try the absolute filepath.
   ![JAR lives here](../../static/img/tutorial_latest_code/jar_lives_here.png)

