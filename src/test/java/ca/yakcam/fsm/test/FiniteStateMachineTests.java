package ca.yakcam.fsm.test;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import ca.yakcam.fsm.FiniteStateMachine;
import ca.yakcam.fsm.StateContext;
import ca.yakcam.fsm.StateException;
import ca.yakcam.fsm.StateMap;
import ca.yakcam.fsm.StateMapException;
import ca.yakcam.fsm.test.state.BeginState;
import ca.yakcam.fsm.test.state.EndState;

public class FiniteStateMachineTests {

    private static StateMap stateMap;

    @BeforeClass
    public static void beforeClass() throws StateMapException {
        stateMap = StateMap.builder()
                .startState("Begin", BeginState.class).status("PROCEED", "End").and()
                .state("End", new EndState()).status("REPEAT", "Begin").exitStatus("EXIT")
                .build();
    }

    @Test
    public void contextContainsValueAfterExecution() throws StateException, StateMapException {
        StateContext context = new StateContext();
        FiniteStateMachine fsm = FiniteStateMachine.fromStateMap(stateMap);
        fsm.execute(context);
        boolean success = (boolean) context.get("SUCCESSFUL");
        assertTrue("Controlling class must be able to access context values after execution.", success);
    }
}
