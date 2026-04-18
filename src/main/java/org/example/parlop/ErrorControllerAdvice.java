package org.example.parlop;

import jakarta.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.RequestContextUtils;

@ControllerAdvice
@Log
public class ErrorControllerAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {
        log.log(Level.WARNING, "Handled ResponseStatusException: " + ex.getReason(), ex);
        flash(req, ex.getReason() != null ? ex.getReason() : "Request could not be completed.");
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, HttpServletRequest req) {
        log.log(Level.SEVERE, "Unhandled exception", ex);
        flash(req, "An unexpected error occurred: " + ex.getMessage());
        return "redirect:/";
    }

    private static void flash(HttpServletRequest req, String msg) {
        var flash = RequestContextUtils.getOutputFlashMap(req);
        if (flash != null) {
            flash.put("errorMessage", msg);
        }
    }
}
