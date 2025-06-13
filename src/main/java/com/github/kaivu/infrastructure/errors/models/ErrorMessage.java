package com.github.kaivu.infrastructure.errors.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class ErrorMessage {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorKey;

    private String message;

    public ErrorMessage(String path, String errorKey, String message) {
        this.path = path;
        this.errorKey = errorKey;
        this.message = message;
    }

    public ErrorMessage(String message) {
        this.path = null;
        this.errorKey = null;
        this.message = message;
    }

    public ErrorMessage(String errorKey, String message) {
        this.path = null;
        this.errorKey = errorKey;
        this.message = message;
    }

    public ErrorMessage() {}
}
