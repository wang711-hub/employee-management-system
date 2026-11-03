package jp.ac.example.employeemanagement.service;

import jp.ac.example.employeemanagement.domain.Department;
import jp.ac.example.employeemanagement.dto.DepartmentForm;
import jp.ac.example.employeemanagement.exception.BusinessRuleException;
import jp.ac.example.employeemanagement.exception.DuplicateDataException;
import jp.ac.example.employeemanagement.exception.ResourceNotFoundException;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import jp.ac.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional
    public Department createDepartment(DepartmentForm form) {
        String name = form.getName().trim();
        if (departmentRepository.existsByName(name)) {
            throw new DuplicateDataException("name", "部署名は既に登録されています。");
        }
        Department department = new Department();
        department.setName(name);
        department.setDescription(normalizeOptional(form.getDescription()));
        try {
            return departmentRepository.saveAndFlush(department);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateDataException("name", "部署名は既に登録されています。");
        }
    }

    @Transactional
    public Department updateDepartment(Long id, DepartmentForm form) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません。"));
        String name = form.getName().trim();
        departmentRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(department.getId())) {
                throw new DuplicateDataException("name", "部署名は既に登録されています。");
            }
        });
        department.setName(name);
        department.setDescription(normalizeOptional(form.getDescription()));
        try {
            return departmentRepository.saveAndFlush(department);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateDataException("name", "部署名は既に登録されています。");
        }
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("部署が見つかりません。"));
        if (employeeRepository.existsByDepartment_Id(id)) {
            throw new BusinessRuleException("社員が所属している部署は削除できません。");
        }
        departmentRepository.delete(department);
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
