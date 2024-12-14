package vn.fpt.web.errors;

import lombok.Getter;
import vn.fpt.constant.AppConstant;
import vn.fpt.constant.EntitiesConstant;
import vn.fpt.constant.ErrorsKeyConstant;
import vn.fpt.utils.ResourceBundleUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    // User Errors
    USER_NOT_FOUND(EntitiesConstant.USER, ErrorsKeyConstant.NOT_FOUND, ""),

    // Ticket Errors
    TICKET_NOT_FOUND(EntitiesConstant.TICKET, ErrorsKeyConstant.NOT_FOUND, ""),

// ... add more cases here ...

;

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
        String messageTemplate =
                ResourceBundleUtil.getKeyWithResourceBundle(AppConstant.I18N_ERROR, locale, getFullKey());
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
