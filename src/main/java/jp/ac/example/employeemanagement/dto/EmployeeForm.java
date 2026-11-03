package jp.ac.example.employeemanagement.dto;

import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class EmployeeForm {

    @NotBlank(message = "社員番号を入力してください。")
    @Size(max = 20, message = "社員番号は20文字以内で入力してください。")
    @Pattern(regexp = "[A-Za-z0-9_-]+", message = "社員番号は半角英数字、ハイフン、アンダースコアで入力してください。")
    private String employeeCode;

    @NotBlank(message = "氏名を入力してください。")
    @Size(max = 100, message = "氏名は100文字以内で入力してください。")
    private String fullName;

    @Email(message = "メールアドレスの形式を確認してください。")
    @Size(max = 150, message = "メールアドレスは150文字以内で入力してください。")
    private String email;

    @Size(max = 30, message = "電話番号は30文字以内で入力してください。")
    private String phone;

    @NotNull(message = "所属部署を選択してください。")
    private Long departmentId;

    @NotBlank(message = "役職を入力してください。")
    @Size(max = 100, message = "役職は100文字以内で入力してください。")
    private String jobTitle;

    @NotNull(message = "入社日を入力してください。")
    private LocalDate hireDate;

    @NotNull(message = "在籍状況を選択してください。")
    private EmployeeStatus status;

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public void setStatus(EmployeeStatus status) {
        this.status = status;
    }
}
