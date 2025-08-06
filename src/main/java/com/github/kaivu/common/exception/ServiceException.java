package com.github.kaivu.common.exception;

import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;

import java.io.Serial;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Getter
@RequestScoped
public class ServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient String entityName;
    private final transient String errorKey;
    private final transient AppErrorEnum errorsEnum;

    public ServiceException(String entityName, String errorKey, String message, AppErrorEnum errorsEnum) {
        super(message);
        this.entityName = entityName;
        this.errorKey = errorKey;
        this.errorsEnum = errorsEnum;
    }

    public ServiceException(AppErrorEnum errorsEnum) {
        super(errorsEnum.getMessage());
        this.entityName = errorsEnum.getEntityName();
        this.errorKey = errorsEnum.getErrorKey();
        this.errorsEnum = errorsEnum;
    }
}
