package jp.ac.example.employeemanagement.controller;

import jp.ac.example.employeemanagement.dto.AiQueryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice(assignableTypes = AiQueryController.class)
public class AiApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AiQueryResponse> invalidQuestion(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().isEmpty()
                ? "質問の入力内容を確認してください。"
                : exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new AiQueryResponse(message, false, "system"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AiQueryResponse> invalidJson(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new AiQueryResponse("リクエスト形式を確認してください。", false, "system"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AiQueryResponse> unexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new AiQueryResponse("AI機能を利用できません。時間をおいて再度お試しください。", false, "system"));
    }
}
