<a href="https://jspinak.github.io/brobot"><img src="https://jspinak.github.io/brobot/img/brobot-landscape4.png" alt="Brobot"></a>

See the [website](https://jspinak.github.io/brobot/) for more details.  

# Introduction

Brobot is a Java-SikuliX-OpenCV framework. It makes it possible to develop 
complex automation applications in areas such as game playing and image-based testing.

# License

Brobot is [MIT licensed](./LICENSE).

The Brobot documentation (e.g., `.md` files in the `/docs` folder) is [Creative Commons licensed](./LICENSE-docs).

# The Desktop Runner

The runner will allow for importing model based automation applications from configuration files.
After importing a configuration, the runner will have a state structure and automation controls that 
appear in its GUI.

It is currently in development. The runner module has a README with more details.

# The Configuration Website

The website will allow you to create state structures and automation instructions and controls visually. 
It will generate a configuration file that can be imported into the desktop runner. 

Work on the website will begin once the runner is in a stable state.

# Brobot Test Organization

## Test Modules

The library's tests are organized into two modules:

### Library Module (`library/`)
- Contains unit tests that verify individual components in isolation
- Does not require Spring Boot context
- Examples: testing methods in isolation, unit tests with mocks
- Location: `library/src/test/java/`

### Library Test Module (`library-test/`)
- Contains integration tests that require a running Spring Boot application
- Tests library functionality in a real application context
- Examples: GUI automation tests, state detection tests, full workflow tests
- Location: `library-test/src/test/java/`

## When to Use Each Module

Place your test in the library module if:
- It tests a single class in isolation
- It uses mocks for all dependencies
- It doesn't require Spring Boot context
- It doesn't need to perform actual GUI operations

Place your test in the library-test module if:
- It requires Spring Boot application context
- It tests integration between multiple components
- It needs to perform actual GUI operations 
- It verifies the library's behavior in a real application