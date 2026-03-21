package com.removebg.exception;

import com.removebg.payment.exception.PaymentException;
import com.removebg.payment.exception.PaymentPackageNotFoundException;
import com.removebg.payment.exception.StripeWebhookException;
import com.removebg.response.RemoveBgResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RemoveBgResponse> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<RemoveBgResponse> handleMaxSize(MaxUploadSizeExceededException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "File too large. Maximum allowed size is 2GB.");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<RemoveBgResponse> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CreditInsufficientException.class)
    public ResponseEntity<RemoveBgResponse> handleCreditInsufficient(CreditInsufficientException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnsupportedFormatException.class)
    public ResponseEntity<RemoveBgResponse> handleUnsupportedFormat(UnsupportedFormatException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OperationFailedException.class)
    public ResponseEntity<RemoveBgResponse> handleOperationFailed(OperationFailedException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RemoveBgResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid request payload.")
                .orElse("Invalid request payload.");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RemoveBgResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(PaymentPackageNotFoundException.class)
    public ResponseEntity<RemoveBgResponse> handlePaymentPackageNotFound(PaymentPackageNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(StripeWebhookException.class)
    public ResponseEntity<RemoveBgResponse> handleStripeWebhookException(StripeWebhookException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<RemoveBgResponse> handlePaymentException(PaymentException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    private ResponseEntity<RemoveBgResponse> buildResponse(HttpStatus status, String message) {
        RemoveBgResponse response = RemoveBgResponse.builder()
                .success(false)
                .statusCode(status)
                .data(message)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
