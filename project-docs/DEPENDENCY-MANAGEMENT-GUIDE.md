# Brobot Dependency Management Guide

## Understanding Gradle Dependency Configurations

This guide explains how Brobot manages its dependencies and why certain choices were made to avoid conflicts while maintaining ease of use.

## Table of Contents
1. [The Problem We're Solving](#the-problem-were-solving)
2. [Gradle Dependency Configurations Explained](#gradle-dependency-configurations-explained)
3. [Brobot's Dependency Strategy](#brobots-dependency-strategy)
4. [Why This Matters for Users](#why-this-matters-for-users)
5. [Troubleshooting Guide](#troubleshooting-guide)

---

## The Problem We're Solving

Brobot faces a unique challenge: it needs to use multiple computer vision libraries that can conflict with each other:

1. **SikuliX** - GUI automation library that bundles its own OpenCV native libraries
2. **JavaCV** - Java wrapper for OpenCV, FFmpeg, and other computer vision libraries
3. **OpenCV** - The underlying computer vision library (different versions in SikuliX vs JavaCV)

### The Conflict Scenario

```
SikuliX 2.0.5 → bundles OpenCV (older version)
     ↓
Brobot library → uses JavaCV → requires OpenCV 4.9.0
     ↓
Your Application → needs both to work together!
```

When both libraries try to load their OpenCV versions, you get errors like:
- `UnsatisfiedLinkError: no opencv_java490.dll`
- `SikuliXception: fatal: problem with native library`

---

## Gradle Dependency Configurations Explained

### 1. `implementation` - Private Dependencies

```gradle
implementation 'some.library:name:1.0'
```

**What it does:**
- The dependency is available to YOUR code
- It is NOT exposed to anyone using your library
- It's completely hidden from downstream users

**When to use:**
- For internal utilities that your code uses
- When you don't want to force this dependency on your users
- To avoid version conflicts in downstream projects

**Example in Brobot:**
```gradle
implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
```
Jackson is used internally for JSON parsing, but users don't need to know about it.

### 2. `api` - Public Dependencies

```gradle
api 'some.library:name:1.0'
```

**What it does:**
- The dependency is available to YOUR code
- It IS exposed to anyone using your library
- Users automatically get this as a transitive dependency

**When to use:**
- When your public methods use types from this dependency
- When users NEED this dependency to use your library
- For core functionality that's part of your API

**Example in Brobot:**
```gradle
api 'org.bytedeco:opencv:4.9.0-1.5.10'
```
Brobot's public methods return `Mat` objects from OpenCV, so users need these classes.

### 3. `compileOnly` - Compile-Time Only

```gradle
compileOnly 'some.library:name:1.0'
```

**What it does:**
- Available ONLY during compilation
- NOT included in the final JAR
- NOT available at runtime
- Users must provide this dependency themselves if needed

**When to use:**
- For optional features
- When the dependency is provided by the runtime environment
- To avoid bundling large libraries

**Example in Brobot:**
```gradle
compileOnly 'org.openjfx:javafx-base:21'
```
JavaFX highlighting is optional - only users who want it need to provide JavaFX.

### 4. `runtimeOnly` - Runtime Only

```gradle
runtimeOnly 'some.library:name:1.0'
```

**What it does:**
- NOT available during compilation
- Only available when the application runs
- Cannot use these classes in your code directly

**When to use:**
- For native libraries that are loaded dynamically
- For JDBC drivers or other runtime plugins
- When you need different implementations on different platforms

**Example in Application:**
```gradle
runtimeOnly 'org.bytedeco:opencv-platform:4.9.0-1.5.10'
```
Native OpenCV libraries are only needed at runtime, not for compilation.

---

## Brobot's Dependency Strategy

### Layer 1: The Brobot Library (What's published to Maven Central)

```gradle
dependencies {
    // SikuliX - Core GUI automation (users always need this)
    api('com.sikulix:sikulixapi:2.0.5') {
        exclude group: 'org.slf4j', module: 'slf4j-nop'
    }
    
    // JavaCV - Just the Java wrapper classes, NO native libraries
    api('org.bytedeco:javacv:1.5.10') {
        exclude group: 'org.bytedeco', module: 'opencv-platform'
        exclude group: 'org.bytedeco', module: 'ffmpeg-platform'
    }
    
    // OpenCV Java API - Just the Java classes (Mat, Rect, etc.)
    api 'org.bytedeco:opencv:4.9.0-1.5.10'
    
    // Spring Framework - Part of Brobot's public API
    api 'org.springframework:spring-context'
    
    // Internal JSON processing - Hidden from users
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    
    // Optional JavaFX - Users provide if needed
    compileOnly 'org.openjfx:javafx-base:21'
}
```

**Why this configuration?**
1. **`api` for SikuliX**: Every Brobot user needs SikuliX functionality
2. **`api` for JavaCV core**: Brobot's public methods use `Mat` and other JavaCV types
3. **NO platform natives**: Avoids conflicts and reduces JAR size (4MB vs 400MB!)
4. **`implementation` for Jackson**: Internal detail users don't need to know about
5. **`compileOnly` for JavaFX**: Optional feature - only some users need it

### Layer 2: Your Application

```gradle
dependencies {
    // Get Brobot and all its api dependencies automatically
    implementation 'io.github.jspinak:brobot'
    
    // Provide platform-specific natives at runtime
    runtimeOnly 'org.bytedeco:opencv-platform:4.9.0-1.5.10'
    runtimeOnly 'org.bytedeco:ffmpeg-platform:6.1.1-1.5.10'
    
    // Optional: Add JavaFX if you want highlighting
    implementation 'org.openjfx:javafx-base:21:win'  // Platform-specific
}
```

**Why this configuration?**
1. **`implementation` for Brobot**: You're using it, not exposing it further
2. **`runtimeOnly` for natives**: Prevents compile-time conflicts, loaded dynamically
3. **Platform-specific natives**: The `-platform` suffix includes all platforms

---

## Why This Matters for Users

### Without This Strategy (The Bad Way)

If Brobot included everything as `api`:
```gradle
// DON'T DO THIS!
api 'org.bytedeco:opencv-platform:4.9.0-1.5.10'  // 400MB of natives!
api 'org.bytedeco:ffmpeg-platform:6.1.1-1.5.10'  // Another 200MB!
```

**Problems:**
1. **Huge download**: Brobot would be 600MB+ instead of 4MB
2. **Version conflicts**: Forces specific versions on all users
3. **Platform issues**: Windows users get Mac/Linux libraries they don't need
4. **SikuliX conflicts**: Two OpenCV versions trying to load simultaneously

### With This Strategy (The Good Way)

**Benefits:**
1. **Small library**: Brobot is only ~4MB
2. **User control**: Users choose their platform and versions
3. **No conflicts**: Runtime resolution avoids compile-time clashes
4. **Works everywhere**: Same configuration works on Windows, Mac, Linux

---

## How Native Libraries Load at Runtime

Here's what happens when your Brobot application starts:

```
1. Application starts
   ↓
2. Spring Boot initializes
   ↓
3. Brobot creates a StateImage with a Pattern
   ↓
4. Pattern needs OpenCV Mat class (Java class - already loaded)
   ↓
5. Mat class needs native OpenCV library (opencv_java490.dll/so)
   ↓
6. JavaCV's Loader class activates:
   - Detects OS (Windows/Mac/Linux)
   - Extracts correct native library from JAR to temp directory
   - Loads native library into JVM
   ↓
7. OpenCV operations now work!
```

This is why `runtimeOnly` works - the native libraries are only needed at step 5, not during compilation.

---

## Troubleshooting Guide

### Error: "UnsatisfiedLinkError: no opencv_java490"

**Cause**: Missing native libraries at runtime

**Solution**: Add to your application's build.gradle:
```gradle
runtimeOnly 'org.bytedeco:opencv-platform:4.9.0-1.5.10'
```

### Error: "cannot access Mat" during compilation

**Cause**: Missing JavaCV classes at compile time

**Solution**: This means Brobot's api dependencies aren't being included. Check:
1. You're using a recent version of Brobot
2. Your repository includes Maven Central

### Error: "fatal: problem with native library"

**Cause**: Conflict between SikuliX and JavaCV OpenCV versions

**Solution**: 
1. Use `runtimeOnly` not `implementation` for platform natives
2. Don't mix different OpenCV versions
3. Let JavaCV's Loader handle library extraction

### Large Application JAR (hundreds of MB)

**Cause**: Including platform natives in your JAR

**Solution**: Use `runtimeOnly` instead of `implementation` for platform dependencies:
```gradle
// WRONG - bundles natives in JAR
implementation 'org.bytedeco:opencv-platform:4.9.0-1.5.10'

// RIGHT - loads natives at runtime
runtimeOnly 'org.bytedeco:opencv-platform:4.9.0-1.5.10'
```

---

## Best Practices for Brobot Applications

### 1. Minimal Dependencies
```gradle
dependencies {
    implementation 'io.github.jspinak:brobot'
    runtimeOnly 'org.bytedeco:opencv-platform:4.9.0-1.5.10'
    runtimeOnly 'org.bytedeco:ffmpeg-platform:6.1.1-1.5.10'
}
```

### 2. Platform-Specific (Smaller Downloads)
```gradle
dependencies {
    implementation 'io.github.jspinak:brobot'
    
    // Only include your platform's natives
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        runtimeOnly 'org.bytedeco:opencv:4.9.0-1.5.10:windows-x86_64'
        runtimeOnly 'org.bytedeco:ffmpeg:6.1.1-1.5.10:windows-x86_64'
    }
}
```

### 3. With Optional Features
```gradle
dependencies {
    implementation 'io.github.jspinak:brobot'
    runtimeOnly 'org.bytedeco:opencv-platform:4.9.0-1.5.10'
    runtimeOnly 'org.bytedeco:ffmpeg-platform:6.1.1-1.5.10'
    
    // Optional: JavaFX for highlighting
    implementation 'org.openjfx:javafx-base:21'
    implementation 'org.openjfx:javafx-graphics:21'
}
```

---

## Summary

The key insight is **separation of concerns**:

1. **Compile time**: Only need Java API classes (`api` dependencies)
2. **Runtime**: Need actual native libraries (`runtimeOnly` dependencies)
3. **Optional features**: Use `compileOnly` to avoid forcing dependencies
4. **Internal details**: Use `implementation` to hide from users

This strategy ensures:
- ✅ Small library size
- ✅ No version conflicts
- ✅ Cross-platform compatibility
- ✅ Easy for users (just add 2 dependencies)
- ✅ No manual OpenCV installation required

By understanding these configurations, you can create libraries that are both powerful and easy to use, without forcing unnecessary dependencies or conflicts on your users.