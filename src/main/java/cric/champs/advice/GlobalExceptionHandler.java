package cric.champs.advice;

import cric.champs.customexceptions.*;
import io.jsonwebtoken.ExpiredJwtException;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.tool.schema.spi.CommandAcceptanceException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.UnexpectedTypeException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(errors -> errorMessage.put(errors.getField(), errors.getDefaultMessage()));
        return errorMessage;
    }

    @ExceptionHandler(TransientDataAccessResourceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleTransientDataAccessResourceException(TransientDataAccessResourceException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }
    @ExceptionHandler(CommandAcceptanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleCommandAcceptanceException(CommandAcceptanceException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNullPointerException(NullPointerException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleMissingServletRequestPartException(MissingServletRequestPartException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleBindException(BindException exception) {
        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("Error Message",exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleFileNotFoundException(FileNotFoundException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(IndexOutOfBoundsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleIndexOutOfBoundsException(IndexOutOfBoundsException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleSQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException exception) {
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("Error Message", exception.getMessage());
        return stringStringMap;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleRuntimeException(RuntimeException exception) {
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("Error Message", exception.getMessage());
        return stringStringMap;
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleDuplicateKeyException() {
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("Error Message", "already present");
        return stringStringMap;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public Map<String, String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHttpMediaTypeNotSupportedException() {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", "HttpMediaTypeNotSupported");
        return errorMessage;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleIOException(IOException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(IdentifierGenerationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleIdentifierGenerationException(IdentifierGenerationException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage() + "\nplease select the photo proper image");
        return errorMessage;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleAccessDeniedException(AccessDeniedException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleIllegalStateException(IllegalStateException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleIllegalArgumentException(IllegalArgumentException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    public Map<String, String> handleExpiredJwtException() {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", "jwt token expired");
        return errorMessage;
    }

    @ExceptionHandler(SQLSyntaxErrorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleSQLSyntaxErrorException(SQLSyntaxErrorException exception) {
        Map<String, String> stringStringMap = new HashMap<>();
        stringStringMap.put("Error Message", exception.getMessage());
        return stringStringMap;
    }

    @ExceptionHandler(LoginFailedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleLoginFailedException(LoginFailedException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(SignupException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleSignupException(SignupException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public Map<String, String> handleTokenExpiredException(TokenExpiredException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(EmailValidationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleEmailValidationException(EmailValidationException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(OTPGenerateException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleOTPGenerateException(OTPGenerateException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(UpdateFailedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUpdateFailedException(UpdateFailedException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(NotVerifiedException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotVerifiedException(NotVerifiedException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(UnexpectedTypeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUnexpectedTypeException(UnexpectedTypeException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(InsufficientTimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleInsufficientTimeException(InsufficientTimeException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(LiveScoreUpdationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleLiveScoreUpdationException(LiveScoreUpdationException exception) {
        Map<String, String> errorMessage = new HashMap<>();
        errorMessage.put("Error Message ", exception.getMessage());
        return errorMessage;
    }
}
