package jp.ac.example.employeemanagement.dto;

public record HireCountResult(boolean departmentExists, String departmentName, String startDate,
                              String endDateExclusive, long count) {
}
