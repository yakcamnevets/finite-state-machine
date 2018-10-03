package ca.yakcam.fsm.test;

import org.junit.Test;

import ca.yakcam.fsm.StateException;
import ca.yakcam.fsm.StateMap;
import ca.yakcam.fsm.StateMapException;

public class StateMapTests {
    @Test
    public void TestAnonymous() throws StateException, StateMapException {
        StateMap stateMap = StateMap.builder()
                .state("Test", (context) -> {
                    context.setStatus("DONE");
                }).exitStatus("DONE").build();
    }

    @Test(expected = StateMapException.class)
    public void DuplicationStateError() throws StateException, StateMapException {
        StateMap stateMap = StateMap.builder()
                .state("Duplicate", (context) -> {
                    context.setStatus("DONE");
                }).exitStatus("DONE")
                .and()
                .state("Duplicate", (context) -> {
                    context.setStatus("DONE");
                })
                .build();
    }
}
