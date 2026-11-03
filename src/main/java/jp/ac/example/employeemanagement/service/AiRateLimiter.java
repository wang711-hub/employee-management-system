package jp.ac.example.employeemanagement.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimiter {

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private final ConcurrentHashMap<String, ArrayDeque<Instant>> requestsByUser = new ConcurrentHashMap<>();
    private final Clock clock;

    public AiRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean allow(String username) {
        ArrayDeque<Instant> requests = requestsByUser.computeIfAbsent(username, key -> new ArrayDeque<>());
        synchronized (requests) {
            Instant now = clock.instant();
            Instant cutoff = now.minus(WINDOW);
            while (!requests.isEmpty() && !requests.peekFirst().isAfter(cutoff)) {
                requests.removeFirst();
            }
            if (requests.size() >= MAX_REQUESTS) {
                return false;
            }
            requests.addLast(now);
            return true;
        }
    }
}
