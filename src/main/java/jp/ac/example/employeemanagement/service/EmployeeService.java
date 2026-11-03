package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.Department;
import jp.ac.example.employeemanagement.domain.Employee;
import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jp.ac.example.employeemanagement.dto.EmployeeForm;
import jp.ac.example.employeemanagement.dto.EmployeeSummary;
import jp.ac.example.employeemanagement.exception.BusinessRuleException;
import jp.ac.example.employeemanagement.exception.DuplicateDataException;
import jp.ac.example.employeemanagement.exception.ResourceNotFoundException;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import jp.ac.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<Employee> searchEmployees(String keyword, Long departmentId,
                                          EmployeeStatus status, Pageable pageable) {
        if (departmentId != null && !departmentRepository.existsById(departmentId)) {
            return Page.empty(pageable);
        }
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return employeeRepository.searchActiveEmployees(normalizedKeyword, departmentId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Employee getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません。"));
        if (employee.isDeleted()) {
            throw new ResourceNotFoundException("社員が見つかりません。");
        }
        return employee;
    }

    @Transactional
    public Employee createEmployee(EmployeeForm form) {
        if (employeeRepository.findByEmployeeCode(form.getEmployeeCode().trim()).isPresent()) {
            throw new DuplicateDataException("employeeCode", "社員番号は既に登録されています。");
        }
        String email = normalizeOptional(form.getEmail());
        if (email != null && employeeRepository.findByEmail(email).isPresent()) {
            throw new DuplicateDataException("email", "メールアドレスは既に登録されています。");
        }

        Employee employee = new Employee();
        employee.setEmployeeCode(form.getEmployeeCode().trim());
        applyForm(employee, form, email);
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateDataException(null, "社員番号またはメールアドレスは既に登録されています。");
        }
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeForm form) {
        Employee employee = getEmployee(id);
        if (!employee.getEmployeeCode().equals(form.getEmployeeCode().trim())) {
            throw new BusinessRuleException("社員番号は変更できません。");
        }
        String email = normalizeOptional(form.getEmail());
        if (email != null) {
            employeeRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(employee.getId())) {
                    throw new DuplicateDataException("email", "メールアドレスは既に登録されています。");
                }
            });
        }
        applyForm(employee, form, email);
        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateDataException(null, "社員番号またはメールアドレスは既に登録されています。");
        }
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getEmployee(id);
        employee.setDeleted(true);
        employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public long countRegisteredEmployees() {
        return employeeRepository.countByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public long countActiveEmployees() {
        return employeeRepository.countByDeletedFalseAndStatus(EmployeeStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countActiveEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.countByDepartment_IdAndDeletedFalseAndStatus(departmentId, EmployeeStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public long countEmployeesByDepartmentId(Long departmentId) {
        return employeeRepository.countByDepartment_IdAndDeletedFalse(departmentId);
    }

    @Transactional(readOnly = true)
    public long countHiredEmployees(LocalDate startDate, LocalDate endDateExclusive) {
        return employeeRepository.countByDeletedFalseAndHireDateGreaterThanEqualAndHireDateLessThan(
                startDate, endDateExclusive);
    }

    @Transactional(readOnly = true)
    public long countHiredEmployeesByDepartment(String departmentName, LocalDate startDate,
                                                LocalDate endDateExclusive) {
        return employeeRepository.countByDepartment_NameAndDeletedFalseAndHireDateGreaterThanEqualAndHireDateLessThan(
                departmentName, startDate, endDateExclusive);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSummary> searchForAi(String nameKeyword, String departmentName, EmployeeStatus status) {
        String normalizedName = normalizeOptional(nameKeyword);
        String normalizedDepartment = normalizeOptional(departmentName);
        return employeeRepository.searchForAi(normalizedName, normalizedDepartment, status,
                        Pageable.ofSize(20))
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeSummary findSummaryByCode(String employeeCode) {
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode.trim())
                .filter(existing -> !existing.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません。"));
        return toSummary(employee);
    }

    private void applyForm(Employee employee, EmployeeForm form, String email) {
        Department department = departmentRepository.findById(form.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("所属部署が見つかりません。"));
        employee.setFullName(form.getFullName().trim());
        employee.setEmail(email);
        employee.setPhone(normalizeOptional(form.getPhone()));
        employee.setDepartment(department);
        employee.setJobTitle(form.getJobTitle().trim());
        employee.setHireDate(form.getHireDate());
        employee.setStatus(form.getStatus());
    }

    private EmployeeSummary toSummary(Employee employee) {
        return new EmployeeSummary(employee.getEmployeeCode(), employee.getFullName(),
                employee.getDepartment().getName(), employee.getJobTitle(), employee.getHireDate(),
                employee.getStatus());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
