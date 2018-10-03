package ca.yakcam.fsm;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class FiniteStateMachine {
    private final StateMap stateMap;

    private FiniteStateMachine(StateMap stateMap) {
        this.stateMap = Objects.requireNonNull(stateMap);
    }

    public StateContext execute() throws StateException, StateMapException {
        StateContext context = new StateContext();
        execute(context);
        return context;
    }

    public StateContext execute(String startStateName) throws StateException, StateMapException {
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

    public void execute(StateContext context, String startStateName) throws StateException, StateMapException {
        if (Objects.isNull(startStateName)) {
            throw new StateException("No start state found.");
        }
        String stateName = startStateName;
        while (!Objects.isNull(stateName)) {
            context.clearStatus();
            context.setStateNode(stateMap.getStateInstances().get(stateName));
            context.getStateNode().execute(context);
            if (Objects.isNull(context.getStatus())) {
                throw new StateMapException("Unable to locate state for null status.");
            }
            Map<String, String> statusMap = stateMap.getStateStatusMap().get(stateName);
            if (Objects.isNull(statusMap)) {
                throw new StateMapException("Unable to find state, no status mapped.");
            }
            stateName = statusMap.get(context.getStatus());
        }
        context.clearStatus();
        context.cleanState();
    }

    public static FiniteStateMachine fromStateMap(StateMap stateMap) {
        return new FiniteStateMachine(stateMap);
    }

    public static FiniteStateMachine fromXmlInputStream(InputStream inputStream) throws StateMapException, ParserConfigurationException, IOException, SAXException {
        StateMap.StateMapBuilder stateMapBuilder = StateMap.builder();
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(inputStream);
        Element rootElement = document.getDocumentElement();

        return new FiniteStateMachine(stateMapBuilder.build());
    }
}
