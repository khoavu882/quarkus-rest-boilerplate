package com.github.kaivu.config.handler;

import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.constant.EntitiesConstant;
import com.github.kaivu.common.constant.ErrorsKeyConstant;
import com.github.kaivu.common.exception.AppErrorEnum;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 3/12/24
 * Time: 10:10 AM
 */
@Getter
@Slf4j
public enum ErrorsEnum implements AppErrorEnum {

    // Auth Errors
    AUTH_UNAUTHORIZED(EntitiesConstant.AUTH, ErrorsKeyConstant.UNAUTHORIZED),
    AUTH_NO_ACCESS(EntitiesConstant.AUTH, ErrorsKeyConstant.PERMISSION_DENIED),
    AUTH_INVALID_REDIRECT(EntitiesConstant.AUTH, ErrorsKeyConstant.INVALID_REDIRECT),

    // System Errors
    SYSTEM_BUNDLE_DOES_NOT_EXIST(EntitiesConstant.SYSTEM, ErrorsKeyConstant.BUNDLE_DOES_NOT_EXIST),
    SYSTEM_CLIENT_BAD_REQUEST(EntitiesConstant.SYSTEM, ErrorsKeyConstant.CLIENT_BAD_REQUEST),
    SYSTEM_INTERNAL_SERVER_ERROR(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INTERNAL_SERVER_ERROR),
    SYSTEM_INVALID_SORT_ORDER(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INVALID_SORT_ORDER),
    SYSTEM_INVALID_SORT_PARAMETER(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INVALID_SORT_PARAMETER),
    SYSTEM_INVALID_TIME_RANGE(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INVALID_TIME_RANGE),

    // Entity Device Errors
    ENTITY_DEVICE_NOT_FOUND(EntitiesConstant.ENTITY_DEVICE, ErrorsKeyConstant.NOT_FOUND),
    ENTITY_DEVICE_NAME_ALREADY_EXISTS(EntitiesConstant.ENTITY_DEVICE, ErrorsKeyConstant.ALREADY_EXISTS),

    // Media/File Streaming Errors
    FILES_RANGE_NOT_SATISFIABLE(EntitiesConstant.FILES, ErrorsKeyConstant.NOT_ACCEPTABLE),

    // User Errors
    USER_NOT_FOUND(EntitiesConstant.USER, ErrorsKeyConstant.NOT_FOUND),
    ;

    private static final Map<String, String> MESSAGE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ErrorsEnum> ENUM_MAP = new HashMap<>();
    private final String entityName;
    private final String errorKey;

    ErrorsEnum(String entityName, String errorKey) {
        this.entityName = entityName;
        this.errorKey = errorKey;
    }

    @Override
    public String getMessage() {
        return getMessage(Locale.ENGLISH);
    }

    @Override
    public String getMessage(Locale locale, Object... args) {
        String messageTemplate =
                MESSAGE_CACHE.computeIfAbsent(getFullKey() + AppConstant.DOT + locale.toString(), key -> {
                    try {
                        return ResourceBundleUtil.getKeyWithResourceBundle(
                                AppConstant.I18N_ERROR, locale, getFullKey());
                    } catch (MissingResourceException ex) {
                        // A missing bundle key must never break the exception mapper that is
                        // rendering this very error — fall back to the enum name instead of throwing.
                        log.error(
                                "Missing i18n key '{}' for locale '{}' — add it to error_messages*.properties",
                                getFullKey(),
                                locale);
                        return getFullKey();
                    }
                });
        return args.length > 0 ? String.format(messageTemplate, args) : messageTemplate;
    }

    // Static block to initialize the enum map
    static {
        for (ErrorsEnum errorsEnum : ErrorsEnum.values()) {
            ENUM_MAP.put(errorsEnum.name(), errorsEnum);
        }
    }

    // Static method to get ErrorsEnum instance by name
    public static ErrorsEnum getByName(String name) {
        return ENUM_MAP.get(name);
    }
}
