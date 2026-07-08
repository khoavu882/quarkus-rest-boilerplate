package com.github.kaivu.common.exception;

import java.util.Locale;

/**
 * Interface for defining error enums with standardized structure.
 * Follows the pattern: entity.error_key for consistent i18n message keys.
 */
public interface AppErrorEnum {

    /**
     * Returns the entity name part of the error key
     */
    String getEntityName();

    /**
     * Returns the error key part of the error key
     */
    String getErrorKey();

    /**
     * Returns the full error key in format: entity.error_key
     */
    default String getFullKey() {
        return getEntityName() + "." + getErrorKey();
    }

    /**
     * Returns the default (English) error message
     */
    String getMessage();

    /**
     * Returns the localized error message, interpolated with the given arguments.
     * Stateless: resolves and formats fresh on every call, never mutates the enum constant.
     */
    String getMessage(Locale locale, Object... args);
}
