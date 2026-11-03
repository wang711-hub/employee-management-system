package jp.ac.example.employeemanagement.repository;

import jp.ac.example.employeemanagement.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    boolean existsByName(String name);

    List<Department> findAllByOrderByNameAsc();
}
