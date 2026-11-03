package jp.ac.example.employeemanagement.service;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Supplier;

@Component
public class AiModelCallGuard implements ClientHttpRequestInterceptor {

    private static final int MAX_MODEL_CALLS_PER_QUERY = 6;
    private final ThreadLocal<Integer> currentQueryCalls = new ThreadLocal<>();

    public <T> T execute(Supplier<T> operation) {
        currentQueryCalls.set(0);
        try {
            return operation.get();
        } finally {
            currentQueryCalls.remove();
        }
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        Integer currentCount = currentQueryCalls.get();
        if (currentCount != null) {
            if (currentCount >= MAX_MODEL_CALLS_PER_QUERY) {
                throw new IOException("AI model call limit exceeded for one query.");
            }
            currentQueryCalls.set(currentCount + 1);
        }
        return execution.execute(request, body);
    }
}
