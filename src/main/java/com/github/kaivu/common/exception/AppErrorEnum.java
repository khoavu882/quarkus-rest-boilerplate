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
     * Returns the error message
     */
    String getMessage();

    /**
     * Returns localized error message with optional extension message
     */
    String getMessage(Locale locale, String extentMessage);

    /**
     * Sets message with locale and arguments
     */
    void setMessageWithExtendMessage(Locale locale, Object... args);

    /**
     * Returns this enum instance with locale and arguments set
     */
    AppErrorEnum withLocale(Locale locale, Object... args);
}
