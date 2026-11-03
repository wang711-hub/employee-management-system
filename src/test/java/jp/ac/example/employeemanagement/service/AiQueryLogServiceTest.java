package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.AiQueryLog;
import jp.ac.example.employeemanagement.repository.AiQueryLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AiQueryLogService.class, jp.ac.example.employeemanagement.config.TimeConfiguration.class})
class AiQueryLogServiceTest {

    @Autowired
    private AiQueryLogService aiQueryLogService;

    @Autowired
    private AiQueryLogRepository aiQueryLogRepository;

    @Autowired
    private Clock applicationClock;

    @Test
    void persistsOnlyOperationStatusDurationAndTimeMetadata() {
        LocalDateTime before = LocalDateTime.now(applicationClock);
        aiQueryLogService.record("AI_QUERY", "POLICY_REFUSAL", 23);
        LocalDateTime after = LocalDateTime.now(applicationClock);

        AiQueryLog log = aiQueryLogRepository.findAll().getFirst();
        assertThat(log.getOperationType()).isEqualTo("AI_QUERY");
        assertThat(log.getResultStatus()).isEqualTo("POLICY_REFUSAL");
        assertThat(log.getDurationMs()).isEqualTo(23);
        assertThat(log.getCreatedAt()).isBetween(before, after);
        assertThat(AiQueryLog.class.getDeclaredFields())
                .extracting(java.lang.reflect.Field::getName)
                .containsExactlyInAnyOrder("id", "operationType", "resultStatus", "durationMs", "createdAt");
    }
}
