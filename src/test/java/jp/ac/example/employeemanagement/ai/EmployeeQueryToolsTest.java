package jp.ac.example.employeemanagement.ai;

import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jp.ac.example.employeemanagement.dto.DepartmentCountResult;
import jp.ac.example.employeemanagement.dto.EmployeeCountResult;
import jp.ac.example.employeemanagement.dto.HireCountResult;
import jp.ac.example.employeemanagement.dto.EmployeeLookupResult;
import jp.ac.example.employeemanagement.dto.EmployeeSummary;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import jp.ac.example.employeemanagement.service.EmployeeService;
import jp.ac.example.employeemanagement.service.AiToolCallTracker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeQueryToolsTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AiToolCallTracker toolCallTracker;

    @InjectMocks
    private EmployeeQueryTools tools;

    @Test
    void activeCountUsesTheDefinedDeletedAndStatusCondition() {
        when(employeeService.countActiveEmployees()).thenReturn(17L);

        EmployeeCountResult result = tools.countActiveEmployees();

        assertThat(result.count()).isEqualTo(17);
        assertThat(result.condition()).contains("deleted=false", "status=ACTIVE");
        verify(toolCallTracker).record("countActiveEmployees");
    }

    @Test
    void departmentCountMakesMissingDepartmentDifferentFromZeroEmployees() {
        when(departmentRepository.existsByName("存在しない部署")).thenReturn(false);

        DepartmentCountResult result = tools.countActiveEmployeesByDepartment("存在しない部署");

        assertThat(result.departmentExists()).isFalse();
        assertThat(result.count()).isZero();
    }

    @Test
    void searchToolForwardsOnlyPermittedFilters() {
        when(employeeService.searchForAi("架空", "開発部", EmployeeStatus.ACTIVE)).thenReturn(java.util.List.of());

        assertThat(tools.searchEmployees("架空", "開発部", "active")).isEmpty();
        verify(employeeService).searchForAi("架空", "開発部", EmployeeStatus.ACTIVE);
    }

    @Test
    void employeeLookupReturnsOnlyTheWhitelistedSummary() {
        EmployeeSummary summary = new EmployeeSummary("EMP0001", "架空社員01", "開発部", "開発担当",
                LocalDate.of(2025, 4, 1), EmployeeStatus.ACTIVE);
        when(employeeService.findSummaryByCode("EMP0001")).thenReturn(summary);

        EmployeeLookupResult result = tools.getEmployeeByCode("EMP0001");

        assertThat(result.found()).isTrue();
        assertThat(result.employee()).isEqualTo(summary);
        assertThat(Arrays.stream(EmployeeSummary.class.getRecordComponents())
                .map(component -> component.getName()))
                .doesNotContain("email", "phone", "passwordHash");
    }

    @Test
    void hireCountRejectsInvalidOrReversedDates() {
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                tools.countHiredEmployees("2025-13-01", "2026-01-01", null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                tools.countHiredEmployees("2026-01-01", "2025-01-01", null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void hireCountUsesHalfOpenDateRangeAndOptionalDepartment() {
        when(employeeService.countHiredEmployeesByDepartment("開発部", LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 1, 1))).thenReturn(3L);

        HireCountResult result = tools.countHiredEmployees("2025-01-01", "2026-01-01", "開発部");

        assertThat(result.departmentExists()).isTrue();
        assertThat(result.departmentName()).isEqualTo("開発部");
        assertThat(result.count()).isEqualTo(3);
        verify(employeeService).countHiredEmployeesByDepartment("開発部", LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 1, 1));
    }
}
