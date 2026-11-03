package jp.ac.example.employeemanagement.dto;

import jp.ac.example.employeemanagement.domain.EmployeeStatus;

import java.time.LocalDate;

public record EmployeeSummary(String employeeCode, String fullName, String departmentName,
                              String jobTitle, LocalDate hireDate, EmployeeStatus status) {
}
