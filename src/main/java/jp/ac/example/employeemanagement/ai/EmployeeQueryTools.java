package jp.ac.example.employeemanagement.ai;

import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jp.ac.example.employeemanagement.dto.DepartmentCountResult;
import jp.ac.example.employeemanagement.dto.EmployeeCountResult;
import jp.ac.example.employeemanagement.dto.EmployeeLookupResult;
import jp.ac.example.employeemanagement.dto.EmployeeSummary;
import jp.ac.example.employeemanagement.dto.HireCountResult;
import jp.ac.example.employeemanagement.exception.ResourceNotFoundException;
import jp.ac.example.employeemanagement.repository.DepartmentRepository;
import jp.ac.example.employeemanagement.service.AiToolCallTracker;
import jp.ac.example.employeemanagement.service.EmployeeService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class EmployeeQueryTools {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
    private final AiToolCallTracker toolCallTracker;

    public EmployeeQueryTools(EmployeeService employeeService, DepartmentRepository departmentRepository,
                              AiToolCallTracker toolCallTracker) {
        this.employeeService = employeeService;
        this.departmentRepository = departmentRepository;
        this.toolCallTracker = toolCallTracker;
    }

    @Tool(description = "未削除で在籍状況が ACTIVE の社員数を返す読み取り専用ツール。社員数の質問で部署指定がない場合に使う。")
    public EmployeeCountResult countActiveEmployees() {
        toolCallTracker.record("countActiveEmployees");
        return new EmployeeCountResult(employeeService.countActiveEmployees(), "deleted=false and status=ACTIVE");
    }

    @Tool(description = "指定された部署の、未削除かつ ACTIVE の社員数を返す読み取り専用ツール。部署名は完全一致。")
    public DepartmentCountResult countActiveEmployeesByDepartment(
            @ToolParam(description = "部署の正確な名前。例: 開発部") String departmentName) {
        toolCallTracker.record("countActiveEmployeesByDepartment");
        String normalizedName = requireText(departmentName, "部署名が必要です。");
        boolean exists = departmentRepository.existsByName(normalizedName);
        long count = exists ? employeeService.countActiveEmployeesByDepartmentId(
                departmentRepository.findByName(normalizedName).orElseThrow().getId()) : 0;
        return new DepartmentCountResult(exists, normalizedName, count, "deleted=false and status=ACTIVE");
    }

    @Tool(description = "指定期間に入社した未削除社員を数える読み取り専用ツール。日付は YYYY-MM-DD、終了日は含まない。部署名は省略可能。")
    public HireCountResult countHiredEmployees(
            @ToolParam(description = "開始日。ISO形式 YYYY-MM-DD") String startDate,
            @ToolParam(description = "終了日。ISO形式 YYYY-MM-DD。この日は含みません。") String endDateExclusive,
            @ToolParam(description = "部署名の完全一致。部署指定がなければ null。", required = false)
            @Nullable String departmentName) {
        toolCallTracker.record("countHiredEmployees");
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDateExclusive);
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("終了日は開始日より後の日付にしてください。");
        }
        String normalizedName = normalizeOptional(departmentName);
        boolean exists = normalizedName == null || departmentRepository.existsByName(normalizedName);
        long count = 0;
        if (exists) {
            count = normalizedName == null
                    ? employeeService.countHiredEmployees(start, end)
                    : employeeService.countHiredEmployeesByDepartment(normalizedName, start, end);
        }
        return new HireCountResult(exists, normalizedName, start.toString(), end.toString(), count);
    }

    @Tool(description = "名前、部署、在籍状況で未削除社員を検索し、最大20件の工号、氏名、部署、役職、入社日、在籍状況だけを返す読み取り専用ツール。")
    public List<EmployeeSummary> searchEmployees(
            @ToolParam(description = "氏名の一部。指定しない場合は null。", required = false) @Nullable String nameKeyword,
            @ToolParam(description = "部署名の完全一致。指定しない場合は null。", required = false) @Nullable String departmentName,
            @ToolParam(description = "ACTIVE, LEAVE, RESIGNED のいずれか。指定しない場合は null。", required = false)
            @Nullable String status) {
        toolCallTracker.record("searchEmployees");
        EmployeeStatus parsedStatus = parseOptionalStatus(status);
        return employeeService.searchForAi(nameKeyword, departmentName, parsedStatus);
    }

    @Tool(description = "社員番号から未削除社員の工号、氏名、部署、役職、入社日、在籍状況だけを返す読み取り専用ツール。")
    public EmployeeLookupResult getEmployeeByCode(
            @ToolParam(description = "社員番号。例: EMP0001") String employeeCode) {
        toolCallTracker.record("getEmployeeByCode");
        try {
            EmployeeSummary employee = employeeService.findSummaryByCode(requireText(employeeCode, "社員番号が必要です。"));
            return new EmployeeLookupResult(true, employee);
        } catch (ResourceNotFoundException exception) {
            return new EmployeeLookupResult(false, null);
        }
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(requireText(value, "日付が必要です。"), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("日付は YYYY-MM-DD 形式で指定してください。");
        }
    }

    private EmployeeStatus parseOptionalStatus(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }
        try {
            return EmployeeStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("在籍状況は ACTIVE、LEAVE、RESIGNED のいずれかです。");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
