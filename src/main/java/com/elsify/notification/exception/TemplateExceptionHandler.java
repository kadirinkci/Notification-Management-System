package com.elsify.notification.exception;

import com.elsify.notification.template.TemplateVariableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TemplateExceptionHandler {

    @ExceptionHandler(TemplateVariableException.class)
    public ProblemDetail handleTemplateVariableException(
            TemplateVariableException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Template variable mismatch");
        problem.setProperty(
                "missingVariables",
                exception.getMissingVariables()
        );
        problem.setProperty(
                "unexpectedVariables",
                exception.getUnexpectedVariables()
        );

        return problem;
    }
}
