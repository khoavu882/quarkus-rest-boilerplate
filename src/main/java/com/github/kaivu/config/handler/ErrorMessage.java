package com.github.kaivu.config.handler;

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
    private String key;

    private String message;

    public ErrorMessage(String path, String key, String message) {
        this.path = path;
        this.key = key;
        this.message = message;
    }

    public ErrorMessage(String message) {
        this.path = null;
        this.key = null;
        this.message = message;
    }

    public ErrorMessage(String key, String message) {
        this.path = null;
        this.key = key;
        this.message = message;
    }

    public ErrorMessage() {}
}
