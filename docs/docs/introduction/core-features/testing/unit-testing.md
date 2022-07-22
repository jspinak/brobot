---
sidebar_position: 3
---

# Unit Testing

In unit testing, it's important that
the results are reproducible since they will be measured against pre-determined,
expected results. This is achieved in Brobot by using screenshots to represent 
an unchanging model of the environment. Unit testing is performed in Brobot as a 
combination of a mock and a real run, and uses screenshots instead of a 
live environment. Find operations are performed with real execution on the specified
screenshots. Setting `BrobotSettings.mock` to `true` will make sure that all other 
actions will be mocked. The screenshots do not have to be on screen to be used with
Find operations; they will be taken from the folder specified by 
`BrobotSettings.screenshotPath`. The default is the folder 
`screenshots` in the root project directory. 
This folder is the same used by the State Structure builder. The default filename in
this folder is `screen`; it can be changed with the variable 
`BrobotSettings.screenshotFilename`.   

Unit tests in Brobot are typically run with a separate Spring Boot executable, or
`@SpringBootApplication` file. They also require their own classes, as in traditional
tests such as JUnit. In the test class, the screenshot to be used should be specified 
in the code. The class TestOutput can be used to compare the results with the 
expected results. Below is a simple example of a unit test in which the screenshot
is set, one method is tested, and the results are compared with the expected results
and printed to the console.  

        BrobotSettings.screenshot = "screen1";
        classToTest.methodToTest();
        testOutput.assertTrue(...);
