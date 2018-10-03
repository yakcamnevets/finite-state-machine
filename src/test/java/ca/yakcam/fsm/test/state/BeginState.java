package ca.yakcam.fsm.test.state;

import ca.yakcam.fsm.StateContext;
import ca.yakcam.fsm.StateNode;

public class BeginState implements StateNode {
    public BeginState() {
    }

    @Override
    public void execute(StateContext context) {
        context.setStatus("PROCEED");
    }
}
