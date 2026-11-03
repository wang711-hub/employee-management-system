package jp.ac.example.employeemanagement.controller;

import jakarta.validation.Valid;
import jp.ac.example.employeemanagement.domain.Employee;
import jp.ac.example.employeemanagement.domain.EmployeeStatus;
import jp.ac.example.employeemanagement.dto.EmployeeForm;
import jp.ac.example.employeemanagement.exception.BusinessRuleException;
import jp.ac.example.employeemanagement.exception.DuplicateDataException;
import jp.ac.example.employeemanagement.exception.ResourceNotFoundException;
import jp.ac.example.employeemanagement.service.DepartmentService;
import jp.ac.example.employeemanagement.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
public class EmployeeController {

    private static final int PAGE_SIZE = 10;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping("/employees")
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long departmentId,
                       @RequestParam(required = false) String status,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        EmployeeStatus selectedStatus = null;
        String filterError = null;
        if (status != null && !status.isBlank()) {
            try {
                selectedStatus = EmployeeStatus.valueOf(status);
            } catch (IllegalArgumentException exception) {
                filterError = "在籍状況の指定が正しくありません。";
            }
        }
        if (keyword != null && keyword.length() > 100) {
            filterError = "検索キーワードは100文字以内で入力してください。";
            keyword = keyword.substring(0, 100);
        }

        PageRequest pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                Sort.by(Sort.Order.asc("employeeCode"), Sort.Order.asc("id")));
        Page<Employee> employees = filterError == null
                ? employeeService.searchEmployees(keyword, departmentId, selectedStatus, pageable)
                : Page.empty(pageable);
        model.addAttribute("employees", employees);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("departmentId", departmentId);
        model.addAttribute("selectedStatus", status == null ? "" : status);
        model.addAttribute("filterError", filterError);
        return "employees/list";
    }

    @GetMapping("/employees/new")
    public String createForm(Model model) {
        model.addAttribute("employeeForm", new EmployeeForm());
        prepareForm(model, false);
        return "employees/form";
    }

    @PostMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("employeeForm") EmployeeForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, false);
            return "employees/form";
        }
        try {
            Employee employee = employeeService.createEmployee(form);
            redirectAttributes.addFlashAttribute("notice", "社員を登録しました。");
            return "redirect:/employees/" + employee.getId();
        } catch (DuplicateDataException exception) {
            rejectDuplicate(bindingResult, exception);
            prepareForm(model, false);
            return "employees/form";
        } catch (ResourceNotFoundException exception) {
            bindingResult.rejectValue("departmentId", "department.notFound", "所属部署が見つかりません。");
            prepareForm(model, false);
            return "employees/form";
        }
    }

    @GetMapping("/employees/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.getEmployee(id));
        return "employees/detail";
    }

    @GetMapping("/employees/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Employee employee = employeeService.getEmployee(id);
        model.addAttribute("employeeForm", toForm(employee));
        model.addAttribute("employeeId", id);
        prepareForm(model, true);
        return "employees/form";
    }

    @PostMapping("/employees/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("employeeForm") EmployeeForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeId", id);
            prepareForm(model, true);
            return "employees/form";
        }
        try {
            employeeService.updateEmployee(id, form);
            redirectAttributes.addFlashAttribute("notice", "社員情報を更新しました。");
            return "redirect:/employees/" + id;
        } catch (DuplicateDataException exception) {
            rejectDuplicate(bindingResult, exception);
            model.addAttribute("employeeId", id);
            prepareForm(model, true);
            return "employees/form";
        } catch (BusinessRuleException exception) {
            bindingResult.reject("employee.update", exception.getMessage());
            model.addAttribute("employeeId", id);
            prepareForm(model, true);
            return "employees/form";
        } catch (ResourceNotFoundException exception) {
            bindingResult.rejectValue("departmentId", "department.notFound", "所属部署が見つかりません。");
            model.addAttribute("employeeId", id);
            prepareForm(model, true);
            return "employees/form";
        }
    }

    @PostMapping("/employees/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        employeeService.deleteEmployee(id);
        redirectAttributes.addFlashAttribute("notice", "社員を削除しました。");
        return "redirect:/employees";
    }

    private void prepareForm(Model model, boolean editing) {
        model.addAttribute("editing", editing);
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("statuses", Arrays.asList(EmployeeStatus.values()));
    }

    private EmployeeForm toForm(Employee employee) {
        EmployeeForm form = new EmployeeForm();
        form.setEmployeeCode(employee.getEmployeeCode());
        form.setFullName(employee.getFullName());
        form.setEmail(employee.getEmail());
        form.setPhone(employee.getPhone());
        form.setDepartmentId(employee.getDepartment().getId());
        form.setJobTitle(employee.getJobTitle());
        form.setHireDate(employee.getHireDate());
        form.setStatus(employee.getStatus());
        return form;
    }

    private void rejectDuplicate(BindingResult bindingResult, DuplicateDataException exception) {
        if (exception.getField() == null) {
            bindingResult.reject("duplicate", exception.getMessage());
        } else {
            bindingResult.rejectValue(exception.getField(), "duplicate", exception.getMessage());
        }
    }
}
