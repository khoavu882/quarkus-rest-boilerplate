package com.github.kaivu.configuration.handler.mapper;

import com.github.kaivu.common.constant.AppHeaderConstant;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
public class NotSupportedExceptionMapper implements ExceptionMapper<NotSupportedException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(NotSupportedException ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);

        log.error(errorId, ex);

        return Response.status(ex.getResponse().getStatus()).build();
    }
}
