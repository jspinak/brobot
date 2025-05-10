---
sidebar_position: 2
---

# Brobot Runner: Desktop Automation Execution Environment

Brobot Runner is a desktop application that serves as the execution environment for GUI automation projects 
created with the Brobot Configuration Website. It provides a robust, user-friendly interface for importing, 
managing, executing, and monitoring automation workflows.

This document outlines features that are a work in progress and may not be fully implemented yet.

## Overview

The Brobot Runner completes the automation lifecycle by taking the visually designed configurations from the 
Configuration Website and transforming them into live, executable automation. While the Configuration Website 
is where you design your automation, the Runner is where you bring it to life.

## Key Responsibilities

### Configuration Management

- **Import and validation:** Load automation configurations exported from the Configuration Website
- **Configuration browsing:** Explore loaded project structures, states, and transitions
- **Local storage:** Maintain a library of imported configurations for offline use
- **Version management:** Track and manage different versions of imported configurations

### Execution Control

- **Lifecycle management:** Start, pause, resume, and stop automation execution
- **Execution parameters:** Configure runtime variables and execution settings
- **Scheduling:** Set up automated execution at specified times
- **Batch processing:** Run multiple automation sequences in sequence

### Monitoring and Visualization

- **Real-time status:** Display current state and active transitions during execution
- **State visualization:** Show graphical representation of the automation's current position
- **Progress tracking:** Monitor completion percentage and estimated time remaining
- **Performance metrics:** Track execution time, success rates, and system resource usage

### Logging and Reporting

- **Comprehensive logging:** Record detailed information about each automation step
- **Screenshot capture:** Automatically capture screenshots at key points in execution
- **Error reporting:** Provide detailed diagnostics when failures occur
- **Results export:** Generate reports in various formats (PDF, HTML, CSV)

## User Interface Components

### Project Navigator

- Browse and manage imported automation projects
- View project metadata and properties
- Access project-specific settings and configurations

### Configuration Viewer

- Explore states, transitions, and automation instructions
- Inspect element properties and images
- Validate configuration integrity

### Execution Dashboard

- Control panel for starting, pausing, and stopping automation
- Status indicators showing current state and activity
- Progress visualization for long-running automation tasks
- Real-time updates on execution metrics

### Log Viewer

- Filterable log display with search functionality
- Color-coded log levels (info, warning, error)
- Timestamp and context information for each log entry
- Screenshot viewer for captured execution snapshots

### Settings Panel

- Application preferences and configuration
- Environment variables and global settings
- Resource management and optimization controls
- Integration settings for external systems

## Technical Architecture

Brobot Runner is built on a modular architecture that separates concerns and promotes maintainability:

- **JavaFX UI Layer:** Provides the user interface and interactive components
- **Configuration Manager:** Handles parsing, validation, and storage of automation configurations
- **Execution Engine:** Interfaces with the Brobot library to execute automation tasks
- **State Manager:** Tracks and manages the current state of automation execution
- **Event System:** Facilitates communication between components through an event-driven architecture
- **Logging Framework:** Captures and organizes detailed execution information

## Workflow: From Configuration to Execution

### Import Configuration

1. Select an exported configuration package from the Configuration Website
2. Validate configuration integrity and completeness
3. Add the configuration to your local library

### Prepare Execution

1. Set execution parameters and variables
2. Configure logging and screenshot settings
3. Define success/failure criteria

### Execute Automation

1. Start the automation sequence
2. Monitor progress in real-time
3. Pause or adjust execution as needed

### Review Results

1. Examine logs and execution metrics
2. Analyze any errors or unexpected behaviors
3. Export reports for documentation or analysis

### Refine (if necessary)

1. Identify areas for improvement
2. Return to the Configuration Website to update the configuration
3. Re-import and test the updated configuration

## Advanced Features

### Mock Mode

- Test automation flows without performing actual GUI interactions
- Validate state transitions and logic
- Identify potential issues before live execution

### Resource Optimization

- Adjust execution speed to balance performance and reliability
- Manage memory usage for image processing
- Configure CPU utilization for background processing

### Distributed Execution

- Run automation across multiple machines
- Coordinate and combine results from distributed runs
- Balance load across available resources

### Custom Extensions

- Create plugins to extend Runner functionality
- Integrate with additional tools and services
- Develop custom reporting and analytics

## System Requirements

### Supported Operating Systems

- Windows 10/11
- macOS 10.14 or later
- Linux (major distributions with desktop environments)

### Hardware Requirements

- **Processor:** Dual-core 2GHz or faster
- **Memory:** 4GB RAM minimum (8GB recommended)
- **Storage:** 500MB available space

### Software Dependencies

- Java Runtime Environment (JRE) 11 or later
- Access privileges for GUI interaction
- Network access for integrations (optional)

## Getting Started

To begin using Brobot Runner:

1. Download and install the application for your platform
2. Export a configuration package from the Brobot Configuration Website
3. Import the configuration package into Brobot Runner
4. Configure execution parameters and settings
5. Run the automation and monitor the results

## Troubleshooting

### Common Issues

- **Configuration Import Failures:** Usually caused by schema validation errors or missing resources
- **Image Recognition Issues:** May require updating reference images
- **Permission Problems:** Ensure the Runner has appropriate system access rights

### Diagnostic Tools

- **Configuration Validator:** Detailed validation of imported configurations
- **Test Mode:** Execute with additional debugging information
- **Environment Inspector:** Analyze the execution environment for potential issues
- **Log Analyzer:** Identify patterns in execution logs that might indicate problems

### Best Practices

- Keep the Runner application updated to the latest version
- Regularly synchronize configurations from the Configuration Website
- Start with simple automation flows and gradually increase complexity
- Use descriptive names for projects, states, and functions
- Document custom parameters and their expected values
- Run critical automations in controlled environments
- Back up configuration packages before making significant changes

---

Brobot Runner completes the model-based GUI automation ecosystem by providing a robust execution 
environment for configurations designed in the Configuration Website. Together, these tools offer a 
comprehensive solution for creating, managing, and executing reliable GUI automation that can adapt 
to complex and changing environments.