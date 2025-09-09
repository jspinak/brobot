# DPI Awareness in Java 21 - The Reality

## The Problem

**System properties set in code DO NOT disable DPI awareness in Java 21**. The JVM's graphics subsystem initializes before any user code runs, making programmatic DPI disabling impossible.

## Why Our Implementation Doesn't Work

Even though we set `sun.java2d.dpiaware=false` in:
- Static initializers
- Spring ApplicationContextInitializer  
- Before any beans are created

**It's still too late!** The JVM has already initialized its graphics subsystem in DPI-aware mode.

## Working Solutions

### Solution 1: JVM Arguments (ONLY RELIABLE METHOD)

```bash
# This MUST be set as JVM arguments, not in code
java -Dsun.java2d.dpiaware=false \
     -Dsun.java2d.uiScale=1.0 \
     -jar brobot.jar
```

**This is the only way to truly disable DPI awareness in Java 21.**

### Solution 2: Gradle/Maven Configuration

#### Gradle (build.gradle)
```groovy
application {
    applicationDefaultJvmArgs = [
        '-Dsun.java2d.dpiaware=false',
        '-Dsun.java2d.uiScale=1.0',
        '-Dsun.java2d.win.uiScale=1.0'
    ]
}

test {
    jvmArgs '-Dsun.java2d.dpiaware=false',
            '-Dsun.java2d.uiScale=1.0'
}
```

#### Maven (pom.xml)
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <jvmArguments>
            -Dsun.java2d.dpiaware=false
            -Dsun.java2d.uiScale=1.0
        </jvmArguments>
    </configuration>
</plugin>
```

### Solution 3: Wrapper Scripts

Create launch scripts for users:

#### Windows (brobot.bat)
```batch
@echo off
java -Dsun.java2d.dpiaware=false -Dsun.java2d.uiScale=1.0 -jar brobot.jar %*
```

#### Linux/Mac (brobot.sh)
```bash
#!/bin/bash
java -Dsun.java2d.dpiaware=false -Dsun.java2d.uiScale=1.0 -jar brobot.jar "$@"
```

### Solution 4: Accept Logical Resolution

Instead of fighting Java 21's DPI awareness, embrace it:

```java
// Configure Brobot to work with logical resolution
Settings.AlwaysResize = 1.25f; // Scale patterns up for 125% displays

// Or use patterns captured at logical resolution
// (Windows Snipping Tool captures at logical resolution)
```

### Solution 5: Runtime Image Scaling (Workaround)

Use `PhysicalResolutionCapture` class to scale captures:

```java
// Captures at logical resolution but scales to physical
BufferedImage physical = PhysicalResolutionCapture.capturePhysical(screen);
```

## The Truth About DPI in Java

### Java 8 (SikuliX IDE)
- Not DPI-aware by default
- Captures at physical resolution
- Simple but doesn't support high-DPI well

### Java 9-20
- Partially DPI-aware
- Behavior varies by version

### Java 21+
- Fully DPI-aware by default
- Captures at logical resolution
- Better for modern apps but incompatible with legacy patterns

## Verification

Run this to check your setup:

```java
public class DPICheck {
    public static void main(String[] args) throws Exception {
        System.out.println("sun.java2d.dpiaware: " + 
            System.getProperty("sun.java2d.dpiaware"));
        
        Robot robot = new Robot();
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage capture = robot.createScreenCapture(
            new Rectangle(size));
        
        System.out.println("Captured: " + capture.getWidth() + "x" + 
            capture.getHeight());
        
        if (capture.getWidth() == 1536) {
            System.out.println("→ DPI-aware (logical resolution)");
            System.out.println("→ Set JVM args to disable!");
        } else if (capture.getWidth() == 1920) {
            System.out.println("→ Not DPI-aware (physical resolution)");
        }
    }
}
```

## Recommendations

### For Brobot Library Users

1. **Best Practice**: Always launch with JVM arguments:
   ```bash
   java -Dsun.java2d.dpiaware=false -jar your-app.jar
   ```

2. **Alternative**: Use patterns captured at logical resolution (Windows tools)

3. **Fallback**: Set `Settings.AlwaysResize = 1.25f` to scale patterns

### For Brobot Library Development

We should:
1. **Document** that JVM arguments are required for physical resolution
2. **Provide** launcher scripts with correct JVM arguments
3. **Detect** DPI mode at runtime and warn users:
   ```java
   if (captureWidth == 1536 && expectedWidth == 1920) {
       logger.warn("Capturing at logical resolution! " +
                  "Add -Dsun.java2d.dpiaware=false to JVM arguments");
   }
   ```

4. **Support** both modes with configuration:
   ```properties
   # Choose capture mode
   brobot.capture.mode=PHYSICAL  # Requires JVM args
   brobot.capture.mode=LOGICAL   # Java 21 default
   brobot.capture.mode=AUTO      # Detect and adapt
   ```

## The Bottom Line

**You cannot programmatically disable DPI awareness in Java 21.** It must be done via:
- JVM command-line arguments
- Launcher scripts
- Build tool configuration

The code we added (`DPIAwarenessDisabler`) will document the attempt but won't actually disable DPI awareness unless the JVM arguments are present.

## What Actually Works

```bash
# This is the ONLY way to get physical resolution captures in Java 21:
java -Dsun.java2d.dpiaware=false -jar brobot.jar

# Without this, you get logical resolution (1536x864 on 125% scaling)
```