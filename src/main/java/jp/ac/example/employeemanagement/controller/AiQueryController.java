package jp.ac.example.employeemanagement.controller;

import jakarta.validation.Valid;
import jp.ac.example.employeemanagement.dto.AiQueryRequest;
import jp.ac.example.employeemanagement.dto.AiQueryResponse;
import jp.ac.example.employeemanagement.dto.AiQueryResult;
import jp.ac.example.employeemanagement.service.AiQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiQueryController {

    private final AiQueryService aiQueryService;

    public AiQueryController(AiQueryService aiQueryService) {
        this.aiQueryService = aiQueryService;
    }

    @PostMapping("/api/ai/query")
    public ResponseEntity<AiQueryResponse> query(@Valid @RequestBody AiQueryRequest request,
                                                  Authentication authentication) {
        AiQueryResult result = aiQueryService.query(request.getQuestion(), authentication.getName());
        return ResponseEntity.status(result.httpStatus()).body(result.response());
    }
}
