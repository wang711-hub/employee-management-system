package jp.ac.example.employeemanagement.dto;

import java.util.List;

public record DashboardSummary(long registeredEmployees, long activeEmployees, long departmentCount,
                               long hiresThisYear, int year, List<DepartmentSummary> departments) {
}
