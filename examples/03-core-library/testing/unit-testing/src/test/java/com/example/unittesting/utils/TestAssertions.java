package com.example.unittesting.utils;

import org.assertj.core.api.AbstractAssert;
import java.util.Map;
import java.util.List;

/**
 * Custom assertions for Brobot testing.
 */
public class TestAssertions {
    
    /**
     * Custom assertions for test states.
     */
    public static class StateAssert extends AbstractAssert<StateAssert, TestDataBuilder.TestState> {
        
        public StateAssert(TestDataBuilder.TestState actual) {
            super(actual, StateAssert.class);
        }
        
        public static StateAssert assertThat(TestDataBuilder.TestState actual) {
            return new StateAssert(actual);
        }
        
        public StateAssert hasName(String expectedName) {
            isNotNull();
            
            if (!actual.name.equals(expectedName)) {
                failWithMessage("Expected state name to be <%s> but was <%s>", 
                    expectedName, actual.name);
            }
            
            return this;
        }
        
        public StateAssert isActive() {
            isNotNull();
            
            if (!actual.isActive) {
                failWithMessage("Expected state to be active but was inactive");
            }
            
            return this;
        }
        
        public StateAssert hasStateObject(String objectName) {
            isNotNull();
            
            if (!actual.stateObjects.containsKey(objectName)) {
                failWithMessage("Expected state to have object <%s> but it was not found", 
                    objectName);
            }
            
            return this;
        }
        
        public StateAssert hasStateObjectCount(int expectedCount) {
            isNotNull();
            
            int actualCount = actual.stateObjects.size();
            if (actualCount != expectedCount) {
                failWithMessage("Expected state to have <%d> objects but had <%d>", 
                    expectedCount, actualCount);
            }
            
            return this;
        }
        
        public StateAssert hasRegion(String regionName) {
            isNotNull();
            
            if (!actual.regions.containsKey(regionName)) {
                failWithMessage("Expected state to have region <%s> but it was not found", 
                    regionName);
            }
            
            return this;
        }
    }
    
    /**
     * Custom assertions for action results.
     */
    public static class ActionResultAssert extends AbstractAssert<ActionResultAssert, TestDataBuilder.TestActionResult> {
        
        public ActionResultAssert(TestDataBuilder.TestActionResult actual) {
            super(actual, ActionResultAssert.class);
        }
        
        public static ActionResultAssert assertThat(TestDataBuilder.TestActionResult actual) {
            return new ActionResultAssert(actual);
        }
        
        public ActionResultAssert isSuccessful() {
            isNotNull();
            
            if (!actual.success) {
                failWithMessage("Expected action to be successful but it failed with: %s", 
                    actual.errorMessage);
            }
            
            return this;
        }
        
        public ActionResultAssert hasFailed() {
            isNotNull();
            
            if (actual.success) {
                failWithMessage("Expected action to fail but it was successful");
            }
            
            return this;
        }
        
        public ActionResultAssert hasErrorMessage(String expectedMessage) {
            isNotNull();
            
            if (actual.errorMessage == null || !actual.errorMessage.contains(expectedMessage)) {
                failWithMessage("Expected error message to contain <%s> but was <%s>", 
                    expectedMessage, actual.errorMessage);
            }
            
            return this;
        }
        
        public ActionResultAssert hasDurationLessThan(long maxDuration) {
            isNotNull();
            
            if (actual.duration >= maxDuration) {
                failWithMessage("Expected duration to be less than <%d>ms but was <%d>ms", 
                    maxDuration, actual.duration);
            }
            
            return this;
        }
        
        public ActionResultAssert hasActionType(String expectedType) {
            isNotNull();
            
            if (!actual.actionType.equals(expectedType)) {
                failWithMessage("Expected action type to be <%s> but was <%s>", 
                    expectedType, actual.actionType);
            }
            
            return this;
        }
    }
    
    /**
     * Custom assertions for users.
     */
    public static class UserAssert extends AbstractAssert<UserAssert, TestDataBuilder.TestUser> {
        
        public UserAssert(TestDataBuilder.TestUser actual) {
            super(actual, UserAssert.class);
        }
        
        public static UserAssert assertThat(TestDataBuilder.TestUser actual) {
            return new UserAssert(actual);
        }
        
        public UserAssert hasUsername(String expectedUsername) {
            isNotNull();
            
            if (!actual.username.equals(expectedUsername)) {
                failWithMessage("Expected username to be <%s> but was <%s>", 
                    expectedUsername, actual.username);
            }
            
            return this;
        }
        
        public UserAssert hasRole(String role) {
            isNotNull();
            
            if (!actual.roles.contains(role)) {
                failWithMessage("Expected user to have role <%s> but roles were %s", 
                    role, actual.roles);
            }
            
            return this;
        }
        
        public UserAssert hasRoleCount(int expectedCount) {
            isNotNull();
            
            int actualCount = actual.roles.size();
            if (actualCount != expectedCount) {
                failWithMessage("Expected user to have <%d> roles but had <%d>", 
                    expectedCount, actualCount);
            }
            
            return this;
        }
        
        public UserAssert hasValidEmail() {
            isNotNull();
            
            if (actual.email == null || !actual.email.contains("@")) {
                failWithMessage("Expected user to have valid email but was <%s>", 
                    actual.email);
            }
            
            return this;
        }
    }
}