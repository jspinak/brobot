---
sidebar_position: 7
title: 'Testing the Automation'
---

# Testing the Automation

A fundamental innovation of model-based GUI automation is its ability to enable systematic testing of the automation code itself. This introduces standard software engineering practices like unit and integration testing into a domain where they were previously considered infeasible.

It is important to distinguish between:
* **Testing Automation**: Testing the automation code itself. This is what the model-based approach enables.
* **Automated Testing**: Using an automation application to test external software.

## Integration Testing Through Mocking

In model-based GUI automation, integration testing is achieved by mocking GUI interactions. During a mock run, the automation does not interact with the live GUI environment (<span class="math-inline">\\Xi</span>, <span class="math-inline">\\Theta</span>).

Instead, it operates on a predefined **Action History (AH)** that contains recorded outcomes of previous GUI interactions.

* The mock action function (`f_a^{mock}`) replaces the live GUI with the set of active states as its input.
* When an action is to be executed, the framework finds a matching historical action from the Action History.
* The historical results are then used as the results of the current action, simulating a live interaction.

This allows for testing the entire system's behavior and state transitions without needing a live, and often stochastic, GUI environment.

## Unit Testing

Unit testing requires deterministic verification of individual components. The framework uses a hybrid approach for this:

* **For observation actions** (like finding an image), the test runs against a fixed scene, such as a static screenshot (<span class="math-inline">\\Xi\_\{x\}</span>). This allows for the validation of real image recognition functionality in a reproducible way.
* **For all other actions** (like clicks or keyboard inputs), the results are simulated using selected historical data from action snapshots, just like in integration testing.

This hybrid model ensures that tests are fully reproducible while still validating critical functionality.

## Model Validation

This testing capability serves a dual purpose. It not only validates the automation instructions but also verifies the accuracy of the state structure (Ω) itself. Mock run failures can point to errors or gaps in the model, such as:

* A missing transition between two states.
* An incomplete set of elements required for a transition.
* An inadequately defined state.

This allows for the iterative improvement and development of an increasingly accurate model of the GUI environment.