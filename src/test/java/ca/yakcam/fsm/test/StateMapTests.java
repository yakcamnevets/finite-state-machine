package ca.yakcam.fsm.test;

import org.junit.Test;

import ca.yakcam.fsm.StateMap;
import ca.yakcam.fsm.StateMapException;

public class StateMapTests {
    @Test
    public void TestAnonymous() throws StateMapException {
        StateMap.builder()
                .state("Test", (context) -> context.setStatus("DONE")).exitStatus("DONE")
                .build();
    }

    @Test(expected = StateMapException.class)
    public void DuplicationStateError() throws StateMapException {
        StateMap.builder()
                .state("Duplicate", (context) -> context.setStatus("DONE")).exitStatus("DONE")
                .and()
                .state("Duplicate", (context) -> context.setStatus("DONE")).exitStatus("DONE")
                .build();
    }
}
