package jp.ac.example.employeemanagement.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiToolCallTrackerTest {

    @Test
    void recordsEachReadOnlyToolOnceAndClearsBetweenQuestions() {
        AiToolCallTracker tracker = new AiToolCallTracker();
        tracker.beginQuery();
        tracker.record("countActiveEmployees");
        tracker.record("countActiveEmployees");
        tracker.record("searchEmployees");

        assertThat(tracker.calledTools()).containsExactly("countActiveEmployees", "searchEmployees");

        tracker.clear();
        assertThat(tracker.calledTools()).isEmpty();
    }
}
