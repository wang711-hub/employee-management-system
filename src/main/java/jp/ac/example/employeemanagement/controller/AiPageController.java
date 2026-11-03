package jp.ac.example.employeemanagement.controller;

import jp.ac.example.employeemanagement.service.AiQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AiPageController {

    private final AiQueryService aiQueryService;

    public AiPageController(AiQueryService aiQueryService) {
        this.aiQueryService = aiQueryService;
    }

    @GetMapping("/ai")
    public String page(Model model) {
        model.addAttribute("aiAvailable", aiQueryService.isAvailable());
        return "ai/index";
    }
}
