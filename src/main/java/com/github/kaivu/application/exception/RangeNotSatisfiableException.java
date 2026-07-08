package com.github.kaivu.application.exception;

import com.github.kaivu.common.exception.AppErrorEnum;
import com.github.kaivu.common.exception.ServiceException;

/**
 * Thrown when an HTTP Range request cannot be satisfied (malformed or out-of-bounds byte range).
 * Maps to 416 Requested Range Not Satisfiable, distinct from the generic 406 Not Acceptable.
 */
public class RangeNotSatisfiableException extends ServiceException {

    public RangeNotSatisfiableException(AppErrorEnum error) {
        super(error);
    }
}
