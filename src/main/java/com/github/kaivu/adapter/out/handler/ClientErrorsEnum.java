package com.github.kaivu.adapter.out.handler;

import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.constant.EntitiesConstant;
import com.github.kaivu.common.constant.ErrorsKeyConstant;
import com.github.kaivu.common.exception.AppErrorEnum;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Khoa Vu.
 * Mail: kai.vu.dev@gmail.com
 * Date: 3/15/25
 * Time: 12:53 AM
 */
@Getter
public enum ClientErrorsEnum implements AppErrorEnum {
    DEMO_REST_CLIENT_BAD_REQUEST(EntitiesConstant.DEMO_REST, ErrorsKeyConstant.CLIENT_BAD_REQUEST),
    DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR(EntitiesConstant.DEMO_REST, ErrorsKeyConstant.INTERNAL_SERVER_ERROR),
    DEMO_REST_PERMISSION_DENIED(EntitiesConstant.DEMO_REST, ErrorsKeyConstant.PERMISSION_DENIED),
    DEMO_REST_UNAUTHORIZED(EntitiesConstant.DEMO_REST, ErrorsKeyConstant.UNAUTHORIZED),
    DEMO_REST_CONFLICT(EntitiesConstant.DEMO_REST, ErrorsKeyConstant.CONFLICT),
    ;

    private static final Map<String, ClientErrorsEnum> ENUM_MAP = new HashMap<>();
    private final String entityName;
    private final String errorKey;

    ClientErrorsEnum(String entityName, String errorKey) {
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
                ResourceBundleUtil.getKeyWithResourceBundleOrFallback(AppConstant.I18N_ERROR, locale, getFullKey());
        return args.length > 0 ? String.format(messageTemplate, args) : messageTemplate;
    }

    // Static block to initialize the enum map
    static {
        for (ClientErrorsEnum errorsEnum : ClientErrorsEnum.values()) {
            ENUM_MAP.put(errorsEnum.name(), errorsEnum);
        }
    }

    // Static method to get ErrorsEnum instance by name
    public static ClientErrorsEnum getByName(String name) {
        return ENUM_MAP.get(name);
    }
}
