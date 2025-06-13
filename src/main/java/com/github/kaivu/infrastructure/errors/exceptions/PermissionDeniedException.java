package com.github.kaivu.infrastructure.errors.exceptions;

import com.github.kaivu.infrastructure.errors.ErrorsEnum;

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
