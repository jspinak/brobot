# Brobot API Documentation Template

## Class Documentation Template

```java
/**
 * Brief one-line description of the class purpose.
 * 
 * <p>Detailed description explaining the class's role in the framework,
 * its key responsibilities, and how it fits into the overall architecture.</p>
 * 
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Feature 1 with explanation</li>
 *   <li>Feature 2 with explanation</li>
 * </ul>
 * 
 * <p><b>Thread Safety:</b> Specify if the class is thread-safe or not</p>
 * 
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Example code showing typical usage
 * MyClass instance = MyClass.builder()
 *     .setSomething("value")
 *     .build();
 * instance.doSomething();
 * }</pre>
 * 
 * @since 1.0
 * @see RelatedClass
 * @author Your Name
 */
```

## Method Documentation Template

```java
/**
 * Brief description of what the method does.
 * 
 * <p>Detailed explanation if needed, including any side effects,
 * preconditions, or important behaviors.</p>
 * 
 * @param paramName Description of the parameter. Include valid ranges,
 *                  null handling, and any constraints.
 * @return Description of the return value. Specify when null might be returned.
 * @throws ExceptionType When this exception is thrown and why
 * 
 * @apiNote This method uses the new setter naming convention (setXxx)
 * @implNote Implementation details that might be useful for maintainers
 * 
 * @since 1.0
 */
```

## Builder Method Documentation

```java
/**
 * Sets the [property name] for this configuration.
 * 
 * @param value The [property description]. Valid range: [specify range].
 *              Default: [specify default]. Must not be null.
 * @return this Builder instance for method chaining
 * 
 * @apiNote Part of the fluent builder API. Use {@code setXxx} naming convention.
 * @since 1.0
 */
public Builder setSomething(String value) {
    this.something = Objects.requireNonNull(value, "value must not be null");
    return self();
}
```

## Enum Documentation

```java
/**
 * Defines the [enum purpose].
 * 
 * <p>This enum is used to [explain usage context].</p>
 * 
 * @since 1.0
 */
public enum MyEnum {
    /**
     * Brief description of this constant.
     * <p>Detailed explanation of when to use this option.</p>
     */
    OPTION_ONE,
    
    /**
     * Brief description of this constant.
     * <p>Detailed explanation of when to use this option.</p>
     */
    OPTION_TWO
}
```

## Package Documentation (package-info.java)

```java
/**
 * Provides [brief description of package purpose].
 * 
 * <p>This package contains [list main components]:</p>
 * <ul>
 *   <li>{@link MainClass} - Primary class for...</li>
 *   <li>{@link HelperClass} - Utility class for...</li>
 * </ul>
 * 
 * <p><b>Package Structure:</b></p>
 * <pre>
 * io.github.jspinak.brobot.action
 * ├── basic/          Core action implementations
 * ├── composite/      Complex multi-step actions  
 * └── internal/       Internal implementation details
 * </pre>
 * 
 * @since 1.0
 */
package io.github.jspinak.brobot.action;
```