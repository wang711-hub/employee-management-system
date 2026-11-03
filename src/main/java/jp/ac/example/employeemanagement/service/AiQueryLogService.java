package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.AiQueryLog;
import jp.ac.example.employeemanagement.repository.AiQueryLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AiQueryLogService {

    private final AiQueryLogRepository repository;
    private final Clock clock;

    public AiQueryLogService(AiQueryLogRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String operationType, String resultStatus, long durationMs) {
        repository.save(new AiQueryLog(operationType, resultStatus, durationMs, clock));
    }
}
