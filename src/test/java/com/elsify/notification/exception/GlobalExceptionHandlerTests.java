package com.elsify.notification.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import com.elsify.notification.web.CorrelationIdFilter;

class GlobalExceptionHandlerTests {

        private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

        @Test
        void unexpectedExceptionReturnsSafeProblemDetail() {
                MockHttpServletRequest request = new MockHttpServletRequest();

                request.setMethod("GET");
                request.setRequestURI("/api/test");
                request.setAttribute(
                                CorrelationIdFilter.REQUEST_ATTRIBUTE,
                                "test-correlation-id");

                ProblemDetail problem = exceptionHandler.handleException(
                                new RuntimeException("Sensitive internal error"),
                                request);

                assertEquals(500, problem.getStatus());
                assertEquals("Internal server error", problem.getTitle());
                assertEquals(
                                "An unexpected error occurred.",
                                problem.getDetail());
                assertEquals(
                                URI.create("/api/test"),
                                problem.getInstance());

                assertNotNull(problem.getProperties());
                assertEquals(
                                ApiErrorCode.INTERNAL_SERVER_ERROR.name(),
                                problem.getProperties().get("errorCode"));
                assertEquals(
                                "test-correlation-id",
                                problem.getProperties().get("correlationId"));
                assertFalse(
                                problem.getDetail()
                                                .contains("Sensitive internal error"));
        }

        @Test
        void serverResponseStatusExceptionDoesNotExposeReason() {
                MockHttpServletRequest request = new MockHttpServletRequest();

                request.setMethod("GET");
                request.setRequestURI("/api/test");
                request.setAttribute(
                                CorrelationIdFilter.REQUEST_ATTRIBUTE,
                                "server-error-correlation-id");

                ProblemDetail problem = exceptionHandler.handleResponseStatusException(
                                new ResponseStatusException(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Sensitive server failure"),
                                request);

                assertEquals(500, problem.getStatus());
                assertEquals(
                                "An unexpected error occurred.",
                                problem.getDetail());
                assertFalse(
                                problem.getDetail()
                                                .contains("Sensitive server failure"));
        }
}
