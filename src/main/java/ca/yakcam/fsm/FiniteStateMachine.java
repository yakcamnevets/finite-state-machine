package ca.yakcam.fsm;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class FiniteStateMachine {
    private final StateMap stateMap;

    private FiniteStateMachine(StateMap stateMap) {
        this.stateMap = Objects.requireNonNull(stateMap);
    }

    public static FiniteStateMachine fromStateMap(StateMap stateMap) {
        return new FiniteStateMachine(stateMap);
    }

    public static FiniteStateMachine fromXml(InputStream xml) throws StateMapException {
        return new FiniteStateMachine(StateMap.builderFromXml(xml).build());
    }

    public StateContext execute() throws StateException, StateMapException {
        StateContext context = new StateContext();
        execute(context);
        return context;
    }

    public StateContext execute(String startStateName) throws StateException {
        StateContext context = new StateContext();
        execute(context, startStateName);
        return context;
    }

    public void execute(StateContext context) throws StateException, StateMapException {
        if (Objects.isNull(stateMap.getDefaultStartStateName())) {
            throw new StateMapException("No default start state found.");
        }
        execute(context, stateMap.getDefaultStartStateName());
    }

    public void execute(StateContext context, String startStateName) throws StateException {
        if (Objects.isNull(startStateName)) {
            throw new StateException("No start state found.");
        }
        String stateName = startStateName;
        while (!Objects.isNull(stateName)) {
            context.clearStatus();
            context.setStateNode(stateMap.getStateInstances().get(stateName));
            context.getStateNode().execute(context);
            if (Objects.isNull(context.getStatus())) {
                throw new StateException("Unable to locate state for null status.");
            }
            Map<String, String> statusMap = stateMap.getStateStatusMap().get(stateName);
            if (Objects.isNull(statusMap)) {
                throw new StateException("Unable to find state, no status mapped.");
            }
            stateName = statusMap.get(context.getStatus());
        }
        context.clearStatus();
        context.cleanState();
    }
}
