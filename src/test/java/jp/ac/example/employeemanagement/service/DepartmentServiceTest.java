package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.Department;
import jp.ac.example.employeemanagement.dto.DepartmentForm;
import jp.ac.example.employeemanagement.dto.EmployeeForm;
import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jp.ac.example.employeemanagement.exception.BusinessRuleException;
import jp.ac.example.employeemanagement.exception.DuplicateDataException;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({DepartmentService.class, EmployeeService.class})
class DepartmentServiceTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void createsAndRenamesDepartmentAndRejectsDuplicate() {
        Department first = departmentService.createDepartment(form("開発部"));
        departmentService.createDepartment(form("人事部"));

        assertThatThrownBy(() -> departmentService.createDepartment(form("開発部")))
                .isInstanceOf(DuplicateDataException.class);

        departmentService.updateDepartment(first.getId(), form("システム開発部"));
        assertThat(departmentRepository.findByName("システム開発部")).isPresent();
    }

    @Test
    void refusesToDeleteDepartmentWithAnyEmployeeReference() {
        Department department = departmentService.createDepartment(form("営業部"));
        EmployeeForm employee = new EmployeeForm();
        employee.setEmployeeCode("EMP0888");
        employee.setFullName("架空社員88");
        employee.setDepartmentId(department.getId());
        employee.setJobTitle("営業担当");
        employee.setHireDate(LocalDate.of(2025, 4, 1));
        employee.setStatus(EmployeeStatus.RESIGNED);
        employeeService.createEmployee(employee);

        assertThatThrownBy(() -> departmentService.deleteDepartment(department.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("社員が所属している部署は削除できません。");
    }

    @Test
    void deletesDepartmentWithNoEmployees() {
        Department department = departmentService.createDepartment(form("経理部"));

        departmentService.deleteDepartment(department.getId());

        assertThat(departmentRepository.existsById(department.getId())).isFalse();
    }

    private DepartmentForm form(String name) {
        DepartmentForm form = new DepartmentForm();
        form.setName(name);
        form.setDescription("架空部署");
        return form;
    }
}
