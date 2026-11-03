package jp.ac.example.employeemanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DepartmentForm {

    @NotBlank(message = "部署名を入力してください。")
    @Size(max = 100, message = "部署名は100文字以内で入力してください。")
    private String name;

    @Size(max = 255, message = "説明は255文字以内で入力してください。")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
