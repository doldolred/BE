package com.doldol.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(String code, String message, List<FieldError> fieldErrors) {
    public record FieldError(String field, String message) { }
    public static ErrorResponse of(ErrorCode errorCode) {return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());}
    public static ErrorResponse of(ErrorCode errorCode, String message) {return new ErrorResponse(errorCode.name(), message, List.of());}
    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {return new ErrorResponse(errorCode.name(), errorCode.getMessage(), fieldErrors);}

}