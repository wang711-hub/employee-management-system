package jp.ac.example.employeemanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_query_logs", indexes = {
        @Index(name = "idx_ai_query_logs_created_at", columnList = "created_at")
})
public class AiQueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", nullable = false, length = 255)
    private String operationType;

    @Column(name = "result_status", nullable = false, length = 32)
    private String resultStatus;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AiQueryLog() {
    }

    public AiQueryLog(String operationType, String resultStatus, long durationMs, Clock clock) {
        this.operationType = operationType;
        this.resultStatus = resultStatus;
        this.durationMs = durationMs;
        this.createdAt = LocalDateTime.now(clock);
    }

    @PrePersist
    protected void setCreatedAtIfMissing() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(Clock.systemDefaultZone());
        }
    }

    public Long getId() {
        return id;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
