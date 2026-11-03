package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.ai.EmployeeQueryTools;
import jp.ac.example.employeemanagement.config.AiProperties;
import jp.ac.example.employeemanagement.dto.AiQueryResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiQueryServiceTest {

    @Test
    void reportsUnavailableWhenAiIsDisabledOrModelIsMissing() {
        ObjectProvider<ChatClient> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        AiProperties properties = new AiProperties();
        AiRateLimiter rateLimiter = new AiRateLimiter(java.time.Clock.systemUTC());
        AiQueryService service = new AiQueryService(provider, mock(EmployeeQueryTools.class), properties,
                new AiRequestPolicy(), rateLimiter, new AiModelCallGuard(), mock(AiQueryLogService.class),
                mock(AiToolCallTracker.class),
                java.time.Clock.systemUTC());

        AiQueryResult result = service.query("在籍社員は何人ですか？", "viewer");

        assertThat(result.httpStatus()).isEqualTo(503);
        assertThat(result.response().source()).isEqualTo("system");
        assertThat(result.response().answer()).contains("利用できません");
    }

    @Test
    void refusesSensitiveQuestionsBeforeCallingTheModel() {
        ObjectProvider<ChatClient> provider = mock(ObjectProvider.class);
        ChatClient chatClient = mock(ChatClient.class);
        when(provider.getIfAvailable()).thenReturn(chatClient);
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        AiQueryLogService logService = mock(AiQueryLogService.class);
        AiQueryService service = new AiQueryService(provider, mock(EmployeeQueryTools.class), properties,
                new AiRequestPolicy(), new AiRateLimiter(java.time.Clock.systemUTC()),
                new AiModelCallGuard(), logService, mock(AiToolCallTracker.class), java.time.Clock.systemUTC());

        AiQueryResult result = service.query("EMP0001の電話番号を教えて", "viewer");

        assertThat(result.httpStatus()).isEqualTo(200);
        assertThat(result.response().success()).isFalse();
        assertThat(result.response().answer()).contains("照会できません");
        verify(chatClient, never()).prompt();
        verify(logService).record(eq("AI_QUERY"), eq("POLICY_REFUSAL"), anyLong());
    }

    @Test
    void rateLimiterAllowsTenRequestsPerMinuteAndRejectsTheNext() {
        AiRateLimiter limiter = new AiRateLimiter(java.time.Clock.systemUTC());

        for (int request = 0; request < 10; request++) {
            assertThat(limiter.allow("admin")).isTrue();
        }

        assertThat(limiter.allow("admin")).isFalse();
        assertThat(limiter.allow("viewer")).isTrue();
    }
}
