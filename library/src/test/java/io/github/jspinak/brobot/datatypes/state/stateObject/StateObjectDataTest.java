package io.github.jspinak.brobot.datatypes.state.stateObject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StateObjectDataTest {

    @Test
    public void testConstructorWithStateObject() {
        // 1. Mock the StateObject interface
        StateObject mockStateObject = mock(StateObject.class);

        // 2. Define the behavior of the mock
        when(mockStateObject.getIdAsString()).thenReturn("ID123");
        when(mockStateObject.getObjectType()).thenReturn(StateObject.Type.IMAGE);
        when(mockStateObject.getName()).thenReturn("TestImage");
        when(mockStateObject.getOwnerStateName()).thenReturn("TestState");
        when(mockStateObject.getOwnerStateId()).thenReturn(1L);

        // 3. Create StateObjectData from the mock
        StateObjectData data = new StateObjectData(mockStateObject);

        // 4. Assert that the data was copied correctly
        assertEquals("ID123", data.getStateObjectId());
        assertEquals(StateObject.Type.IMAGE, data.getObjectType());
        assertEquals("TestImage", data.getStateObjectName());
        assertEquals("TestState", data.getOwnerStateName());
        assertEquals(1L, data.getOwnerStateId());
    }

    @Test
    public void testDefaultConstructor() {
        StateObjectData data = new StateObjectData();
        assertEquals("", data.getStateObjectId());
        assertEquals(StateObject.Type.IMAGE, data.getObjectType());
        assertEquals("", data.getStateObjectName());
        assertEquals("", data.getOwnerStateName());
        assertNull(data.getOwnerStateId());
    }

    @Test
    public void testToString() {
        StateObjectData data = new StateObjectData();
        data.setStateObjectName("MyObject");
        data.setObjectType(StateObject.Type.REGION);
        data.setOwnerStateName("MyState");
        data.setStateObjectId("ID567");
        data.setOwnerStateId(10L);

        String expected = "StateObject: MyObject, REGION, ownerState=MyState, id=ID567, owner state id=10";
        assertEquals(expected, data.toString());
    }
}