package ca.yakcam.fsm;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class StateMap {
    private final Map<String, StateNode> stateInstances;
    private final Map<String, Map<String, String>> stateStatusMap;
    private final String defaultStartStateName;

    private static final String STATE_NAME_ATTRIBUTE_NAME = "name";
    private static final String STATE_CLASS_ATTRIBUTE_NAME = "class";
    private static final String STATE_START_ATTRIBUTE_NAME = "start";
    private static final String STATUS_NAME_ATTRIBUTE_NAME = "name";
    private static final String STATUS_STATE_ELEMENT_NAME = "state";

    private StateMap(Map<String, StateNode> stateInstances, Map<String, Map<String, String>> stateStatusMap, String defaultStartStateName) {
        this.stateInstances = stateInstances;
        this.stateStatusMap = stateStatusMap;
        this.defaultStartStateName = defaultStartStateName;
    }

    public static StateMapBuilder builder() {
        return new StateMapBuilder();
    }

    public static StateMapBuilder builderFromXml(InputStream xml) throws StateMapException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try (InputStream xsd = FiniteStateMachine.class.getResourceAsStream("state-map.xsd")) {
            Schema schema = schemaFactory.newSchema(new StreamSource(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
        } catch (IOException e) {
            throw new StateMapException("Unable to read StateMap schema XSD.", e);
        } catch (SAXException e) {
            throw new StateMapException("StateMap XML is not valid.", e);
        }
        StateMap.StateMapBuilder stateMapBuilder = StateMap.builder();
        Document document;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.parse(xml);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new StateMapException("Unable to read StateMap XML.", e);
        }
        Element stateMapElement = document.getDocumentElement();
        NodeList stateNodes = stateMapElement.getChildNodes();
        for (int i = 0; i < stateNodes.getLength(); i++) {
            Node stateNode = stateNodes.item(i);
            String stateName = stateNode.getAttributes().getNamedItem(STATE_NAME_ATTRIBUTE_NAME).getNodeValue();
            String stateClassName = stateNode.getAttributes().getNamedItem(STATE_CLASS_ATTRIBUTE_NAME).getNodeValue();
            boolean start = Boolean.getBoolean(stateNode.getAttributes().getNamedItem(STATE_START_ATTRIBUTE_NAME).getNodeValue());
            Class<? extends StateNode> stateClass;
            try {
                stateClass = Class.forName(stateClassName).asSubclass(StateNode.class);
            } catch (ClassNotFoundException e) {
                throw new StateMapException(String.format("Could not create state class for name %s", stateClassName), e);
            }
            StateBuilder stateBuilder = stateMapBuilder.state(stateName, stateClass, start);
            NodeList statusNodes = stateNode.getChildNodes();
            for (int j = 0; j < statusNodes.getLength(); j++) {
                Node statusNode = statusNodes.item(i);
                String statusName = stateNode.getAttributes().getNamedItem(STATUS_NAME_ATTRIBUTE_NAME).getNodeValue();
                String statusStateName = stateNode.getAttributes().getNamedItem(STATUS_STATE_ELEMENT_NAME).getNodeValue();
                stateBuilder.status(statusName, statusStateName.equals("") ? null : statusStateName);
            }
            stateMapBuilder = stateBuilder.and();
        }
        return stateMapBuilder;
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

        public StateBuilder state(String stateName, StateNode stateNodeInstance) throws StateMapException {
            return state(stateName, stateNodeInstance, false);
        }

        public StateBuilder state(String stateName, Class<? extends StateNode> stateClass) throws StateMapException {
            return state(stateName, stateClass, false);
        }

        public StateBuilder startState(String stateName, StateNode stateNodeInstance) throws StateMapException {
            return state(stateName, stateNodeInstance, true);
        }

        public StateBuilder startState(String stateName, Class<? extends StateNode> stateClass) throws StateMapException {
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

        public StateBuilder state(String stateName, Class<? extends StateNode> stateClass, boolean isDefaultStartState) throws StateMapException {
            Objects.requireNonNull(stateClass, "Argument stateClass is a required to be non-null.");
            StateNode stateNodeInstance;
            try {
                stateNodeInstance = stateClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new StateMapException("StateNode class must contain a public default constructor.", e);
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
                        throw new StateMapException(String.format("No state %s defined for for status %s.", statusEntry.getValue(), statusEntry.getKey()));
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
