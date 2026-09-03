package com.elsify.notification.template;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public class TemplateVariableException extends RuntimeException {

    private final List<String> missingVariables;
    private final List<String> unexpectedVariables;

    public TemplateVariableException(
            Collection<String> missingVariables,
            Collection<String> unexpectedVariables
    ) {
        super("Template variables do not match");
        this.missingVariables = List.copyOf(missingVariables);
        this.unexpectedVariables = List.copyOf(unexpectedVariables);
    }
}
