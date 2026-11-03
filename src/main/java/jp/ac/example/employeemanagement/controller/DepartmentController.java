package jp.ac.example.employeemanagement.controller;

import jakarta.validation.Valid;
import jp.ac.example.employeemanagement.dto.DepartmentForm;
import jp.ac.example.employeemanagement.dto.DepartmentListItem;
import jp.ac.example.employeemanagement.exception.BusinessRuleException;
import jp.ac.example.employeemanagement.exception.DuplicateDataException;
import jp.ac.example.employeemanagement.service.DepartmentService;
import jp.ac.example.employeemanagement.service.EmployeeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class DepartmentController {

    private final DepartmentService departmentService;
    private final EmployeeService employeeService;

    public DepartmentController(DepartmentService departmentService, EmployeeService employeeService) {
        this.departmentService = departmentService;
        this.employeeService = employeeService;
    }

    @GetMapping("/departments")
    public String list(Model model) {
        populateList(model, new DepartmentForm());
        return "departments/list";
    }

    @PostMapping("/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@Valid @ModelAttribute("departmentForm") DepartmentForm form,
                         BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateList(model, form);
            return "departments/list";
        }
        try {
            departmentService.createDepartment(form);
            redirectAttributes.addFlashAttribute("notice", "部署を登録しました。");
        } catch (DuplicateDataException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/departments";
    }

    @PostMapping("/departments/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("departmentForm") DepartmentForm form,
                         BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "部署名と説明の入力内容を確認してください。");
            return "redirect:/departments";
        }
        try {
            departmentService.updateDepartment(id, form);
            redirectAttributes.addFlashAttribute("notice", "部署情報を更新しました。");
        } catch (DuplicateDataException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/departments";
    }

    @PostMapping("/departments/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("notice", "部署を削除しました。");
        } catch (BusinessRuleException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/departments";
    }

    private void populateList(Model model, DepartmentForm form) {
        List<DepartmentListItem> rows = departmentService.findAll().stream()
                .map(department -> new DepartmentListItem(department.getId(), department.getName(),
                        department.getDescription(), employeeService.countEmployeesByDepartmentId(department.getId())))
                .toList();
        model.addAttribute("departments", rows);
        model.addAttribute("departmentForm", form);
    }
}
