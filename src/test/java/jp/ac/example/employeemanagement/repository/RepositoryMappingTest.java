package jp.ac.example.employeemanagement.repository;

import jp.ac.example.employeemanagement.domain.AppUser;
import jp.ac.example.employeemanagement.domain.Department;
import jp.ac.example.employeemanagement.domain.Employee;
import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepositoryMappingTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void savesAndReadsEntitiesWithDepartmentAndEnumMapping() {
        Department department = new Department();
        department.setName("開発部");
        department.setDescription("架空の開発部署");
        department = departmentRepository.saveAndFlush(department);

        Employee employee = new Employee();
        employee.setEmployeeCode("EMP0001");
        employee.setFullName("テスト社員01");
        employee.setEmail("employee01@example.invalid");
        employee.setDepartment(department);
        employee.setJobTitle("ソフトウェアエンジニア");
        employee.setHireDate(LocalDate.of(2025, 4, 1));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee = employeeRepository.saveAndFlush(employee);

        AppUser user = new AppUser();
        user.setUsername("test-admin");
        user.setPasswordHash("test-hash-only");
        user.setRole("ADMIN");
        user.setEnabled(true);
        appUserRepository.saveAndFlush(user);

        Employee loaded = employeeRepository.findByEmployeeCode("EMP0001").orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(loaded.getDepartment().getName()).isEqualTo("開発部");
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(appUserRepository.findByUsername("test-admin")).isPresent();
    }

    @Test
    void defaultEmployeePageAndCountsCanExcludeSoftDeletedRows() {
        Department department = new Department();
        department.setName("総務部");
        departmentRepository.saveAndFlush(department);

        Employee active = employee("EMP0002", "テスト社員02", department, false);
        Employee deleted = employee("EMP0003", "テスト社員03", department, true);
        employeeRepository.save(active);
        employeeRepository.saveAndFlush(deleted);

        assertThat(employeeRepository.countByDeletedFalse()).isEqualTo(1);
        assertThat(employeeRepository.countByDeletedFalseAndStatus(EmployeeStatus.ACTIVE)).isEqualTo(1);
        assertThat(employeeRepository.findByDeletedFalse(PageRequest.of(0, 10)).getContent())
                .extracting(Employee::getEmployeeCode)
                .containsExactly("EMP0002");
    }

    private Employee employee(String code, String name, Department department, boolean deleted) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFullName(name);
        employee.setDepartment(department);
        employee.setJobTitle("テスト担当");
        employee.setHireDate(LocalDate.of(2025, 4, 1));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDeleted(deleted);
        return employee;
    }
}
