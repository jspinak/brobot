package io.github.jspinak.brobot.dsl;

/**
 * Custom exceptions for DSL-related errors
 */
public class DSLException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String details;

    public enum ErrorCode {
        INVALID_SYNTAX,
        UNKNOWN_ACTION,
        INVALID_OPTION,
        MISSING_REQUIRED_FIELD,
        INVALID_OBJECT_COLLECTION,
        PARSING_ERROR,
        ENCODING_ERROR,
        UNSUPPORTED_OPERATION
    }

    public DSLException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public DSLException(ErrorCode errorCode, String message, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public DSLException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public DSLException(ErrorCode errorCode, String message, String details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "DSLException{" +
                "errorCode=" + errorCode +
                ", message='" + getMessage() + '\'' +
                (details != null ? ", details='" + details + '\'' : "") +
                '}';
    }
}
