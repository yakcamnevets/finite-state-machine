package ca.yakcam.fsm;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StateMap {
    private final Map<String, StateNode> stateInstances;
    private final Map<String, Map<String, String>> stateStatusMap;
    private final String defaultStartStateName;

    private StateMap(Map<String, StateNode> stateInstances, Map<String, Map<String, String>> stateStatusMap, String defaultStartStateName) {
        this.stateInstances = stateInstances;
        this.stateStatusMap = stateStatusMap;
        this.defaultStartStateName = defaultStartStateName;
    }

    public static StateMapBuilder builder() {
        return new StateMapBuilder();
    }

    Map<String, StateNode> getStateInstances() {
        return stateInstances;
    }

    Map<String, Map<String, String>> getStateStatusMap() {
        return stateStatusMap;
    }

    public String getDefaultStartStateName() {
        return defaultStartStateName;
    }

    public StateNode getDefaultStartState() {
        return stateInstances.getOrDefault(defaultStartStateName, null);
    }

    public static class StateMapBuilder {
        private final Map<String, StateNode> stateInstances;
        private final Map<String, Map<String, String>> stateStatusMap;
        private String defaultStartStateName;

        private StateMapBuilder() {
            stateInstances = new HashMap<>();
            stateStatusMap = new HashMap<>();
            defaultStartStateName = null;
        }

        public StateBuilder state(String stateName, StateNode stateNodeInstance) throws StateException, StateMapException {
            return state(stateName, stateNodeInstance, false);
        }

        public StateBuilder state(String stateName, Class<? extends StateNode> stateClass) throws StateException, StateMapException {
            return state(stateName, stateClass, false);
        }

        public StateBuilder startState(String stateName, StateNode stateNodeInstance) throws StateException, StateMapException {
            return state(stateName, stateNodeInstance, true);
        }

        public StateBuilder startState(String stateName, Class<? extends StateNode> stateClass) throws StateException, StateMapException {
            return state(stateName, stateClass, true);
        }

        public StateBuilder state(String stateName, StateNode stateNodeInstance, boolean isDefaultStartState) throws StateMapException {
            Objects.requireNonNull(stateName, "Argument stateName is a required to be non-null.");
            Objects.requireNonNull(stateNodeInstance, "Argument stateNodeInstance is a required to be non-null.");
            if (stateInstances.containsKey(stateName)) {
                throw new StateMapException("StateNode has aready been created.");
            }
            stateInstances.put(stateName, stateNodeInstance);
            setDefaultStartStateName(stateName);
            return new StateBuilder(stateName, this);
        }

        public StateBuilder state(String stateName, Class<? extends StateNode> stateClass, boolean isDefaultStartState) throws StateException, StateMapException {
            Objects.requireNonNull(stateClass, "Argument stateClass is a required to be non-null.");
            StateNode stateNodeInstance = null;
            try {
                stateNodeInstance = stateClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new StateException("StateNode class must contain a public default constructor.", e);
            }
            return state(stateName, stateNodeInstance, isDefaultStartState);
        }

        public StateMap build() throws StateMapException {
            validate();
            return new StateMap(stateInstances, stateStatusMap, defaultStartStateName);
        }

        private void setDefaultStartStateName(String stateName) throws StateMapException {
            if (!Objects.isNull(defaultStartStateName)) {
                throw new StateMapException("Default start state is already set.");
            }
            if (!Objects.equals(stateName, defaultStartStateName)) {
                defaultStartStateName = Objects.requireNonNull(stateName);
            }
        }

        private StateMapBuilder stateStatusMap(String stateName, Map<String, String> statusMap) {
            stateStatusMap.put(stateName, statusMap);
            return this;
        }

        private void validate() throws StateMapException {
            if (stateInstances.isEmpty()) {
                throw new StateMapException("StateMap must contain at least one state.");
            }
            for (Map.Entry<String, Map<String, String>> stateEntry : stateStatusMap.entrySet()) {
                for (Map.Entry<String, String> statusEntry : stateEntry.getValue().entrySet()) {
                    if (!stateInstances.containsKey(statusEntry.getValue())) {
                        throw new StateMapException(String.format("No state '%s' defined for for status '%s'.", statusEntry.getValue(), statusEntry.getKey()));
                    }
                }
            }
            // TODO Validate that there is an exit.
            // TODO Validate that there every path has an exit.
        }
    }

    public static class StateBuilder {
        private Map<String, String> statusMap;
        private String stateName;
        private StateMapBuilder parentStateMapBuilder;

        private StateBuilder(String stateName, StateMapBuilder parentStateMapBuilder) {
            statusMap = new HashMap<>();
            this.stateName = stateName;
            this.parentStateMapBuilder = parentStateMapBuilder;
        }

        public StateBuilder status(String status, String stateName) {
            statusMap.put(status, stateName);
            return this;
        }

        public StateBuilder exitStatus(String status) {
            statusMap.put(status, null);
            return this;
        }

        public StateBuilder start() throws StateMapException {
            parentStateMapBuilder.setDefaultStartStateName(stateName);
            return this;
        }

        public StateMapBuilder and() {
            return parentStateMapBuilder.stateStatusMap(this.stateName, this.statusMap);
        }

        public StateMap build() throws StateMapException {
            return and().build();
        }
    }

}
