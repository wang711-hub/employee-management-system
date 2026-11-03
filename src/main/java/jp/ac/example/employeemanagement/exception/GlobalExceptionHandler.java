package jp.ac.example.employeemanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = {
        jp.ac.example.employeemanagement.controller.EmployeeController.class,
        jp.ac.example.employeemanagement.controller.DepartmentController.class,
        jp.ac.example.employeemanagement.controller.DashboardController.class,
        jp.ac.example.employeemanagement.controller.AuthController.class
})
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException exception) {
        ModelAndView view = new ModelAndView("error/404");
        view.setStatus(HttpStatus.NOT_FOUND);
        view.addObject("message", exception.getMessage());
        return view;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception exception) {
        ModelAndView view = new ModelAndView("error/500");
        view.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        view.addObject("message", "処理中にエラーが発生しました。時間をおいて再度お試しください。");
        return view;
    }
}
