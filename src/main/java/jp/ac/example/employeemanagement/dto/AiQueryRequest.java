package jp.ac.example.employeemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiQueryRequest {

    @NotBlank(message = "質問を入力してください。")
    @Size(max = 500, message = "質問は500文字以内で入力してください。")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
