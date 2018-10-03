package ca.yakcam.fsm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class StateContext implements Map<String, Object> {
    private final Map<String, Object> values;
    private String status;
    private StateNode stateNode;

    public StateContext() {
        values = new HashMap<>();
        clearStatus();
        cleanState();
    }

    public StateContext(Map<String, Object> values) {
        this();
        values.putAll(values);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = Objects.requireNonNull(status, "Context status cannot be null.");
    }

    void clearStatus() {
        status = null;
    }

    public StateNode getStateNode() {
        return stateNode;
    }

    void setStateNode(StateNode stateNode) {
        this.stateNode = stateNode;
    }

    void cleanState() {
        setStateNode(null);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return values.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return values.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return values.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        values.putAll(map);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public Set<String> keySet() {
        return values.keySet();
    }

    @Override
    public Collection<Object> values() {
        return values.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return values.entrySet();
    }
}
