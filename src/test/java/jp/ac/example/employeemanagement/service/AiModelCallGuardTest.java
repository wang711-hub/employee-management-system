package jp.ac.example.employeemanagement.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiModelCallGuardTest {

    @Test
    void capsExternalModelCallsForOneQuestion() throws IOException {
        AiModelCallGuard guard = new AiModelCallGuard();
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        guard.execute(() -> {
            for (int call = 0; call < 6; call++) {
                try {
                    guard.intercept(request, new byte[0], execution);
                } catch (IOException exception) {
                    throw new AssertionError(exception);
                }
            }
            assertThatThrownBy(() -> guard.intercept(request, new byte[0], execution))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("call limit");
            return null;
        });
    }
}
