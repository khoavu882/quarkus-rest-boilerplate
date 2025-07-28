package com.github.kaivu.configuration.handler;

import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.constant.EntitiesConstant;
import com.github.kaivu.common.constant.ErrorsKeyConstant;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 3/12/24
 * Time: 10:10 AM
 */
@Getter
public enum ErrorsEnum {

    // Auth Errors
    AUTH_UNAUTHORIZED(EntitiesConstant.AUTH, ErrorsKeyConstant.UNAUTHORIZED, ""),
    AUTH_NO_ACCESS(EntitiesConstant.AUTH, ErrorsKeyConstant.PERMISSION_DENIED, ""),
    AUTH_INVALID_REDIRECT(EntitiesConstant.AUTH, ErrorsKeyConstant.INVALID_REDIRECT, ""),

    // System Errors
    SYSTEM_BUNDLE_DOES_NOT_EXIST(EntitiesConstant.SYSTEM, ErrorsKeyConstant.BUNDLE_DOES_NOT_EXIST, ""),
    SYSTEM_CLIENT_BAD_REQUEST(EntitiesConstant.SYSTEM, ErrorsKeyConstant.CLIENT_BAD_REQUEST, ""),
    SYSTEM_INTERNAL_SERVER_ERROR(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INTERNAL_SERVER_ERROR, ""),
    SYSTEM_INVALID_SORT_ORDER(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INVALID_SORT_ORDER, ""),
    SYSTEM_INVALID_SORT_PARAMETER(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INVALID_SORT_PARAMETER, ""),
    SYSTEM_INVALID_TIME_RANGE(EntitiesConstant.SYSTEM, ErrorsKeyConstant.INVALID_TIME_RANGE, ""),

    // Entity Device Errors
    ENTITY_DEVICE_NOT_FOUND(EntitiesConstant.ENTITY_DEVICE, ErrorsKeyConstant.NOT_FOUND, ""),
    ENTITY_DEVICE_NAME_ALREADY_EXISTS(EntitiesConstant.ENTITY_DEVICE, ErrorsKeyConstant.ALREADY_EXISTS, ""),

    // User Errors
    USER_NOT_FOUND(EntitiesConstant.USER, ErrorsKeyConstant.NOT_FOUND, ""),
    ;

    private static final Map<String, String> MESSAGE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ErrorsEnum> ENUM_MAP = new HashMap<>();
    private final String entityName;
    private final String errorKey;
    private String message;

    public String getMessage(Locale locale, String extentMessage) {
        String bundleMessage =
                ResourceBundleUtil.getKeyWithResourceBundle(AppConstant.I18N_ERROR, locale, getFullKey());
        return extentMessage != null ? bundleMessage + extentMessage : bundleMessage;
    }

    public void setMessageWithExtendMessage(Locale locale, Object... args) {
        String messageTemplate = MESSAGE_CACHE.computeIfAbsent(
                getFullKey() + AppConstant.DOT + locale.toString(),
                key -> ResourceBundleUtil.getKeyWithResourceBundle(AppConstant.I18N_ERROR, locale, getFullKey()));
        if (args.length > 0) {
            this.message = String.format(messageTemplate, args);
        } else {
            this.message = messageTemplate;
        }
    }

    public String getFullKey() {
        return this.entityName + "." + this.errorKey;
    }

    ErrorsEnum(String entityName, String errorKey, String message) {
        this.entityName = entityName;
        this.errorKey = errorKey;
        this.message = message != null ? message : "";
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

    public ErrorsEnum withLocale(Locale locale, Object... args) {
        this.setMessageWithExtendMessage(locale, args);
        return this;
    }
}
