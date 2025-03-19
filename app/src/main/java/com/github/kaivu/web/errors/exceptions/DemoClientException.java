package com.github.kaivu.web.errors.exceptions;

import com.github.kaivu.web.errors.ClientErrorsEnum;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/15/25
 * Time: 1:04 AM
 */
@Getter
@ToString
public class DemoClientException extends ClientException {

    Response response;

    public DemoClientException(String entityName, String errorKey, Throwable cause, Response response) {
        super(entityName, errorKey, cause);
        this.response = response;
    }

    public DemoClientException(ClientErrorsEnum errorsEnum, Throwable cause, Response response) {
        super(errorsEnum, cause);
        this.response = response;
    }

    public DemoClientException(ClientErrorsEnum errorsEnum, Response response) {
        super(errorsEnum, null);
        this.response = response;
    }

    public DemoClientException(Response response) {
        super(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST, null);
        this.response = response;
    }
}
