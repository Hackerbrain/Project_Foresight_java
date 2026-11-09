package com.weintern.inventory.web;

import com.weintern.inventory.exception.BusinessException;
import com.weintern.inventory.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/** Turns uncaught failures into a readable page instead of a stack trace. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView notFound(ResourceNotFoundException ex) {
        return page(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView business(BusinessException ex) {
        return page(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ModelAndView conflict() {
        return page(HttpStatus.CONFLICT, "Someone else changed this record while you were working. Go back, reload and try again.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView integrity() {
        return page(HttpStatus.CONFLICT, "That change conflicts with existing data (for example a duplicate value or a record still in use).");
    }

    private ModelAndView page(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("message", message);
        return mav;
    }
}
