package com.elsify.notification.exception;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.elsify.notification.template.TemplateVariableException;
import com.elsify.notification.web.CorrelationIdFilter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(TemplateVariableException.class)
        public ProblemDetail handleTemplateVariableException(
                        TemplateVariableException exception,
                        HttpServletRequest request) {
                ProblemDetail problem = createProblem(
                                HttpStatus.BAD_REQUEST,
                                "Template variable mismatch",
                                exception.getMessage(),
                                ApiErrorCode.TEMPLATE_VARIABLE_MISMATCH,
                                request);

                problem.setProperty(
                                "missingVariables",
                                exception.getMissingVariables());
                problem.setProperty(
                                "unexpectedVariables",
                                exception.getUnexpectedVariables());

                return problem;
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ProblemDetail handleMethodArgumentNotValid(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {
                List<Map<String, String>> violations = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> Map.of(
                                                "field", error.getField(),
                                                "message", Objects.requireNonNullElse(
                                                                error.getDefaultMessage(),
                                                                "Invalid value")))
                                .toList();

                ProblemDetail problem = createProblem(
                                HttpStatus.BAD_REQUEST,
                                "Request validation failed",
                                "One or more request fields are invalid.",
                                ApiErrorCode.VALIDATION_ERROR,
                                request);

                problem.setProperty("violations", violations);

                return problem;
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ProblemDetail handleHttpMessageNotReadable(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {
                return createProblem(
                                HttpStatus.BAD_REQUEST,
                                "Malformed request",
                                "Request body is missing or contains invalid JSON.",
                                ApiErrorCode.MALFORMED_REQUEST,
                                request);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ProblemDetail handleMethodArgumentTypeMismatch(
                        MethodArgumentTypeMismatchException exception,
                        HttpServletRequest request) {
                return createProblem(
                                HttpStatus.BAD_REQUEST,
                                "Invalid request parameter",
                                "Request parameter '%s' has an invalid value."
                                                .formatted(exception.getName()),
                                ApiErrorCode.INVALID_REQUEST,
                                request);
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ProblemDetail handleResponseStatusException(
                        ResponseStatusException exception,
                        HttpServletRequest request) {
                HttpStatusCode status = exception.getStatusCode();
                if (status.is5xxServerError()) {
                        return handleException(exception, request);
                }
                HttpStatus resolvedStatus = HttpStatus.resolve(status.value());

                String title = resolvedStatus != null
                                ? resolvedStatus.getReasonPhrase()
                                : "Request failed";

                String detail = exception.getReason() != null
                                ? exception.getReason()
                                : "Request could not be completed.";

                ApiErrorCode errorCode = switch (status.value()) {
                        case 404 -> ApiErrorCode.RESOURCE_NOT_FOUND;
                        case 409 -> ApiErrorCode.RESOURCE_CONFLICT;
                        default -> status.is5xxServerError()
                                        ? ApiErrorCode.INTERNAL_SERVER_ERROR
                                        : ApiErrorCode.INVALID_REQUEST;
                };

                return createProblem(
                                status,
                                title,
                                detail,
                                errorCode,
                                request);
        }

        @ExceptionHandler(Exception.class)
        public ProblemDetail handleException(
                        Exception exception,
                        HttpServletRequest request) {
                if (exception instanceof ErrorResponse errorResponse
                                && !errorResponse.getStatusCode().is5xxServerError()) {
                        HttpStatusCode status = errorResponse.getStatusCode();
                        ProblemDetail originalProblem = errorResponse.getBody();
                        HttpStatus resolvedStatus = HttpStatus.resolve(status.value());

                        String title = originalProblem.getTitle() != null
                                        && !originalProblem.getTitle().isBlank()
                                                        ? originalProblem.getTitle()
                                                        : resolvedStatus != null
                                                                        ? resolvedStatus.getReasonPhrase()
                                                                        : "Request failed";

                        String detail = originalProblem.getDetail() != null
                                        && !originalProblem.getDetail().isBlank()
                                                        ? originalProblem.getDetail()
                                                        : "Request could not be completed.";

                        ApiErrorCode errorCode = switch (status.value()) {
                                case 404 -> ApiErrorCode.RESOURCE_NOT_FOUND;
                                case 409 -> ApiErrorCode.RESOURCE_CONFLICT;
                                default -> ApiErrorCode.INVALID_REQUEST;
                        };

                        return createProblem(
                                        status,
                                        title,
                                        detail,
                                        errorCode,
                                        request);
                }

                String correlationId = getCorrelationId(request);

                log.error(
                                "Unexpected request failure: correlationId={}, method={}, path={}",
                                correlationId,
                                request.getMethod(),
                                request.getRequestURI(),
                                exception);

                return createProblem(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Internal server error",
                                "An unexpected error occurred.",
                                ApiErrorCode.INTERNAL_SERVER_ERROR,
                                request);
        }

        private ProblemDetail createProblem(
                        HttpStatusCode status,
                        String title,
                        String detail,
                        ApiErrorCode errorCode,
                        HttpServletRequest request) {
                ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);

                problem.setTitle(title);
                problem.setType(createProblemType(errorCode));
                problem.setInstance(URI.create(request.getRequestURI()));
                problem.setProperty("errorCode", errorCode.name());
                problem.setProperty(
                                "correlationId",
                                getCorrelationId(request));
                problem.setProperty("timestamp", Instant.now());

                return problem;
        }

        private URI createProblemType(ApiErrorCode errorCode) {
                String typeName = errorCode.name()
                                .toLowerCase(Locale.ROOT)
                                .replace('_', '-');

                return URI.create("urn:problem:" + typeName);
        }

        private String getCorrelationId(HttpServletRequest request) {
                Object correlationId = request.getAttribute(
                                CorrelationIdFilter.REQUEST_ATTRIBUTE);

                if (correlationId instanceof String value) {
                        return value;
                }

                return "unknown";
        }
}
