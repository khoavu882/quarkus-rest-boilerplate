package com.github.kaivu.common.exception;

import lombok.AccessLevel;
import lombok.Getter;

import java.io.Serial;
import java.util.Locale;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Getter
public class ServiceException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient String entityName;
    private final transient String errorKey;
    private final transient AppErrorEnum errorsEnum;

    @Getter(AccessLevel.NONE)
    private transient Locale locale = Locale.ENGLISH;

    @Getter(AccessLevel.NONE)
    private transient Object[] args = new Object[0];

    public ServiceException(AppErrorEnum errorsEnum) {
        this.entityName = errorsEnum.getEntityName();
        this.errorKey = errorsEnum.getErrorKey();
        this.errorsEnum = errorsEnum;
    }

    /**
     * Sets the locale used to resolve the message. Mutates this exception instance only —
     * never the shared {@link AppErrorEnum} constant — so concurrent requests can't overwrite
     * each other's message.
     */
    public ServiceException withLocale(Locale locale) {
        this.locale = locale != null ? locale : Locale.ENGLISH;
        return this;
    }

    /**
     * Sets the interpolation arguments used to resolve the message.
     */
    public ServiceException withArgs(Object... args) {
        this.args = args;
        return this;
    }

    @Override
    public String getMessage() {
        return errorsEnum.getMessage(locale, args);
    }
}
