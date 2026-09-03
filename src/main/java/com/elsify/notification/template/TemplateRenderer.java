package com.elsify.notification.template;

import com.elsify.notification.domain.NotificationTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{\\{([A-Za-z][A-Za-z0-9_]*)\\}\\}");

    public RenderedTemplate render(
            NotificationTemplate template,
            Map<String, String> variables
    ) {
        Map<String, String> suppliedVariables =
                variables == null ? Map.of() : variables;

        Set<String> expectedVariables = new TreeSet<>();
        expectedVariables.addAll(findVariables(template.getSubject()));
        expectedVariables.addAll(findVariables(template.getBody()));

        Set<String> providedVariables =
                new TreeSet<>(suppliedVariables.keySet());

        Set<String> missingVariables = new TreeSet<>(expectedVariables);
        missingVariables.removeAll(providedVariables);

        Set<String> unexpectedVariables = new TreeSet<>(providedVariables);
        unexpectedVariables.removeAll(expectedVariables);

        if (!missingVariables.isEmpty() || !unexpectedVariables.isEmpty()) {
            throw new TemplateVariableException(
                    missingVariables,
                    unexpectedVariables
            );
        }

        return new RenderedTemplate(
                template.getChannel(),
                renderText(template.getSubject(), suppliedVariables),
                renderText(template.getBody(), suppliedVariables)
        );
    }

    private Set<String> findVariables(String text) {
        if (text == null) {
            return Set.of();
        }

        Set<String> variables = new HashSet<>();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);

        while (matcher.find()) {
            variables.add(matcher.group(1));
        }

        return variables;
    }

    private String renderText(
            String text,
            Map<String, String> variables
    ) {
        if (text == null) {
            return null;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = variables.get(variableName);

            matcher.appendReplacement(
                    result,
                    Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
