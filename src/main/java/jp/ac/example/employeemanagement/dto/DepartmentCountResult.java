package jp.ac.example.employeemanagement.dto;

public record DepartmentCountResult(boolean departmentExists, String departmentName, long count,
                                    String condition) {
}
