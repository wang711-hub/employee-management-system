package jp.ac.example.employeemanagement.repository;

import jp.ac.example.employeemanagement.domain.AiQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiQueryLogRepository extends JpaRepository<AiQueryLog, Long> {
}
