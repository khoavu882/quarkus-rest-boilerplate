package com.github.kaivu.infrastructure.errors.exceptions;

import com.github.kaivu.infrastructure.errors.ClientErrorsEnum;
import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/15/25
 * Time: 12:52 AM
 */
@Getter
@ToString
@RequestScoped
public class ClientException extends RuntimeException {

    private final String entityName;

    private final String errorKey;

    private final ClientErrorsEnum errorsEnum;

    private final Boolean passThrough;

    private final String clientMessage;

    public ClientException(String entityName, String errorKey, Throwable cause) {
        super(cause);
        this.entityName = entityName;
        this.errorKey = errorKey;
        this.errorsEnum = null;
        this.passThrough = false;
        this.clientMessage = null;
    }

    public ClientException(ClientErrorsEnum errorsEnum, Throwable cause) {
        super(cause);
        this.entityName = errorsEnum.getEntityName();
        this.errorKey = errorsEnum.getErrorKey();
        this.errorsEnum = errorsEnum;
        this.passThrough = false;
        this.clientMessage = null;
    }

    public ClientException(ClientErrorsEnum errorsEnum, Boolean passThrough, String clientMessage, Throwable cause) {
        super(cause);
        this.entityName = errorsEnum.getEntityName();
        this.errorKey = errorsEnum.getErrorKey();
        this.errorsEnum = errorsEnum;
        this.passThrough = passThrough;
        this.clientMessage = clientMessage;
    }
}
