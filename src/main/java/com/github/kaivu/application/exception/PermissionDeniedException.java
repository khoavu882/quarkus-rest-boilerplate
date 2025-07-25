package com.github.kaivu.application.exception;

import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.configuration.handler.ErrorsEnum;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
public class PermissionDeniedException extends ServiceException {

    public PermissionDeniedException(ErrorsEnum error) {
        super(error.getEntityName(), error.getErrorKey(), error.getMessage(), error);
    }
}
