package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.Department;
import jp.ac.example.employeemanagement.domain.Employee;
import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jp.ac.example.employeemanagement.dto.DashboardSummary;
import jp.ac.example.employeemanagement.dto.DepartmentSummary;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import jp.ac.example.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({EmployeeService.class, DashboardService.class, jp.ac.example.employeemanagement.config.TimeConfiguration.class})
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private Clock applicationClock;

    @Test
    void countsRegistrationActiveDepartmentAndCurrentYearHiresWithDefinedRules() {
        Department development = department("開発部");
        Department hr = department("人事部");
        int currentYear = LocalDate.now(applicationClock).getYear();
        saveEmployee("EMP1001", development, EmployeeStatus.ACTIVE, false,
                LocalDate.of(currentYear, Month.MARCH, 1));
        saveEmployee("EMP1002", hr, EmployeeStatus.RESIGNED, false,
                LocalDate.of(currentYear - 1, Month.MARCH, 1));
        saveEmployee("EMP1003", development, EmployeeStatus.ACTIVE, true,
                LocalDate.of(currentYear, Month.APRIL, 1));

        DashboardSummary summary = dashboardService.getSummary();

        assertThat(summary.registeredEmployees()).isEqualTo(2);
        assertThat(summary.activeEmployees()).isEqualTo(1);
        assertThat(summary.departmentCount()).isEqualTo(2);
        assertThat(summary.hiresThisYear()).isEqualTo(1);
        assertThat(summary.departments()).extracting(DepartmentSummary::employeeCount).containsExactly(1L, 0L);
    }

    private Department department(String name) {
        Department department = new Department();
        department.setName(name);
        return departmentRepository.saveAndFlush(department);
    }

    private void saveEmployee(String code, Department department, EmployeeStatus status, boolean deleted,
                              LocalDate hireDate) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFullName("架空社員" + code);
        employee.setDepartment(department);
        employee.setJobTitle("テスト担当");
        employee.setHireDate(hireDate);
        employee.setStatus(status);
        employee.setDeleted(deleted);
        employeeRepository.save(employee);
    }
}
