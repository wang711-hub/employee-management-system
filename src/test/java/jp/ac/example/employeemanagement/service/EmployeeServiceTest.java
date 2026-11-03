package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.Department;
import jp.ac.example.employeemanagement.domain.Employee;
import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jp.ac.example.employeemanagement.dto.EmployeeForm;
import jp.ac.example.employeemanagement.exception.DuplicateDataException;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import jp.ac.example.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({EmployeeService.class, DepartmentService.class})
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;

    @BeforeEach
    void setUp() {
        department = new Department();
        department.setName("開発部");
        department = departmentRepository.saveAndFlush(department);
    }

    @Test
    void createsEmployeeAndRejectsDuplicateCode() {
        Employee created = employeeService.createEmployee(form("EMP0099", "テスト社員99"));

        assertThat(employeeService.getEmployee(created.getId()).getFullName()).isEqualTo("テスト社員99");
        assertThatThrownBy(() -> employeeService.createEmployee(form("EMP0099", "別の社員")))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("社員番号は既に登録されています。");
    }

    @Test
    void updatesEmployeeAndKeepsEmployeeCodeImmutable() {
        Employee created = employeeService.createEmployee(form("EMP0098", "テスト社員98"));
        EmployeeForm update = form("EMP0098", "更新後社員");
        update.setStatus(EmployeeStatus.LEAVE);

        employeeService.updateEmployee(created.getId(), update);

        assertThat(employeeService.getEmployee(created.getId()).getFullName()).isEqualTo("更新後社員");
        assertThat(employeeService.getEmployee(created.getId()).getStatus()).isEqualTo(EmployeeStatus.LEAVE);
        update.setEmployeeCode("EMP0001");
        assertThatThrownBy(() -> employeeService.updateEmployee(created.getId(), update))
                .hasMessage("社員番号は変更できません。");
    }

    @Test
    void softDeletedEmployeeIsExcludedFromSearchAndCounts() {
        Employee created = employeeService.createEmployee(form("EMP0097", "削除対象社員"));

        employeeService.deleteEmployee(created.getId());

        assertThat(employeeService.countRegisteredEmployees()).isZero();
        assertThat(employeeService.searchEmployees(null, null, null, PageRequest.of(0, 10)).getContent()).isEmpty();
        assertThatThrownBy(() -> employeeService.getEmployee(created.getId()))
                .hasMessage("社員が見つかりません。");
        assertThat(employeeRepository.findByEmployeeCode("EMP0097").orElseThrow().isDeleted()).isTrue();
    }

    @Test
    void aiSearchReturnsAtMostTwentyRowsAndOnlySummaryFields() {
        for (int number = 1; number <= 25; number++) {
            employeeService.createEmployee(form(String.format("EMP%04d", number), "架空社員" + number));
        }

        assertThat(employeeService.searchForAi(null, null, null)).hasSize(20);
    }

    private EmployeeForm form(String employeeCode, String name) {
        EmployeeForm form = new EmployeeForm();
        form.setEmployeeCode(employeeCode);
        form.setFullName(name);
        form.setDepartmentId(department.getId());
        form.setJobTitle("テスト担当");
        form.setHireDate(LocalDate.of(2025, 4, 1));
        form.setStatus(EmployeeStatus.ACTIVE);
        return form;
    }
}
