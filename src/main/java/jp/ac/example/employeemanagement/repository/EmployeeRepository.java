package jp.ac.example.employeemanagement.repository;

import jp.ac.example.employeemanagement.domain.Employee;
import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Override
    @EntityGraph(attributePaths = "department")
    Optional<Employee> findById(Long id);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    Page<Employee> findByDeletedFalse(Pageable pageable);

    long countByDeletedFalse();

    long countByDeletedFalseAndStatus(EmployeeStatus status);

    long countByDepartment_IdAndDeletedFalseAndStatus(Long departmentId, EmployeeStatus status);

    boolean existsByDepartment_Id(Long departmentId);

    long countByDepartment_IdAndDeletedFalse(Long departmentId);

    long countByDeletedFalseAndHireDateGreaterThanEqualAndHireDateLessThan(
            LocalDate startDate, LocalDate endDateExclusive);

    long countByDepartment_NameAndDeletedFalseAndHireDateGreaterThanEqualAndHireDateLessThan(
            String departmentName, LocalDate startDate, LocalDate endDateExclusive);

    @Query("""
            select e from Employee e
            where e.deleted = false
              and (:keyword is null or lower(e.employeeCode) like lower(concat('%', :keyword, '%'))
                   or lower(e.fullName) like lower(concat('%', :keyword, '%')))
              and (:departmentId is null or e.department.id = :departmentId)
              and (:status is null or e.status = :status)
            """)
    @EntityGraph(attributePaths = "department")
    Page<Employee> searchActiveEmployees(@Param("keyword") String keyword,
                                         @Param("departmentId") Long departmentId,
                                         @Param("status") EmployeeStatus status,
                                         Pageable pageable);

    @Query("""
            select e from Employee e
            where e.deleted = false
              and (:nameKeyword is null or lower(e.fullName) like lower(concat('%', :nameKeyword, '%')))
              and (:departmentName is null or e.department.name = :departmentName)
              and (:status is null or e.status = :status)
            order by e.employeeCode asc, e.id asc
            """)
    List<Employee> searchForAi(@Param("nameKeyword") String nameKeyword,
                               @Param("departmentName") String departmentName,
                               @Param("status") EmployeeStatus status,
                               Pageable pageable);
}
