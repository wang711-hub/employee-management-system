package jp.ac.example.employeemanagement.service;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class AiToolCallTracker {

    private final ThreadLocal<Set<String>> calls = ThreadLocal.withInitial(LinkedHashSet::new);

    public void beginQuery() {
        calls.get().clear();
    }

    public void record(String toolName) {
        calls.get().add(toolName);
    }

    public List<String> calledTools() {
        return List.copyOf(calls.get());
    }

    public void clear() {
        calls.remove();
    }
}
