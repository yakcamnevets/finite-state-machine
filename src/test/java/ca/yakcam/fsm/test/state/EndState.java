package ca.yakcam.fsm.test.state;

import ca.yakcam.fsm.StateContext;
import ca.yakcam.fsm.StateNode;

public class EndState implements StateNode {
    public EndState() {
    }

    @Override
    public void execute(StateContext context) {
        String wow = (String) context.get("WOW");
        context.put("SUCCESSFUL", wow.equals("WOW"));
        context.setStatus("EXIT");
    }
}