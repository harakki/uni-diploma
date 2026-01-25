package dev.harakki.comics.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Resource not found -> ResourceNotFoundException -> 404 Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource Not Found");
        return problemDetail;
    }

    // Resource already exists or in use -> ResourceAlreadyExistsException, ResourceInUseException -> 409 Conflict
    @ExceptionHandler({ResourceAlreadyExistsException.class, ResourceInUseException.class})
    public ProblemDetail handleConflictException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Conflict");
        return problemDetail;
    }

    // Access denied -> AccessDeniedException -> 403 Forbidden
//    @ExceptionHandler(AccessDeniedException.class)
//    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
//        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
//                HttpStatus.FORBIDDEN,
//                ex.getMessage()
//        );
//        problemDetail.setTitle("Access Denied");
//        return problemDetail;
//    }

    // Resource not uploaded -> ResourceNotUploadedException -> 400 Bad Request
    @ExceptionHandler(ResourceNotUploadedException.class)
    public ProblemDetail handleBadRequestException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Bad Request");
        return problemDetail;
    }

    // Validation errors -> MethodArgumentNotValidException -> 400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        String detail = "Validation failed for " + ex.getBindingResult().getErrorCount() + " field(s).";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setTitle("Validation Failed");

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (msg1, msg2) -> msg1
                ));
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    // Invalid sort property -> PropertyReferenceException -> 400 Bad Request
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handlePropertyReferenceException(PropertyReferenceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Sorting Property");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    // Invalid UUID -> MethodArgumentTypeMismatchException -> 400 Bad Request
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter: " + ex.getName()
        );
        problemDetail.setTitle("Invalid Parameter");
        return problemDetail;
    }

    // Exception -> 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred."
        );
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }

}
