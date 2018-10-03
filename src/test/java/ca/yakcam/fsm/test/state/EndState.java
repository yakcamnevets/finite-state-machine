package ca.yakcam.fsm.test.state;

import ca.yakcam.fsm.StateNode;
import ca.yakcam.fsm.StateContext;

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