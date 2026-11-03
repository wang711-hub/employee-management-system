package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.Department;
import jp.ac.example.employeemanagement.dto.DashboardSummary;
import jp.ac.example.employeemanagement.dto.DepartmentSummary;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

@Service
public class DashboardService {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
    private final Clock clock;

    public DashboardService(EmployeeService employeeService, DepartmentRepository departmentRepository, Clock clock) {
        this.employeeService = employeeService;
        this.departmentRepository = departmentRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardSummary getSummary() {
        LocalDate today = LocalDate.now(clock);
        LocalDate startOfYear = LocalDate.of(today.getYear(), Month.JANUARY, 1);
        LocalDate startOfNextYear = startOfYear.plusYears(1);
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc();
        List<DepartmentSummary> departmentSummaries = departments.stream()
                .map(department -> new DepartmentSummary(department.getId(), department.getName(),
                        department.getDescription(),
                        employeeService.countActiveEmployeesByDepartmentId(department.getId())))
                .toList();
        return new DashboardSummary(employeeService.countRegisteredEmployees(),
                employeeService.countActiveEmployees(), departments.size(),
                employeeService.countHiredEmployees(startOfYear, startOfNextYear),
                today.getYear(), departmentSummaries);
    }
}
