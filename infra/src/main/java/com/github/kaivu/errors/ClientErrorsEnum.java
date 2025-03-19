package com.github.kaivu.errors;

import com.github.kaivu.constant.EntitiesConstant;
import com.github.kaivu.constant.ErrorsKeyConstant;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/15/25
 * Time: 12:53 AM
 */
@Getter
public enum ClientErrorsEnum {
    DEMO_REST_CLIENT_BAD_REQUEST(EntitiesConstant.DEMO_REST, ErrorsKeyConstant.CLIENT_BAD_REQUEST),
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

    public String getFullKey() {
        return this.entityName + "." + this.errorKey;
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
