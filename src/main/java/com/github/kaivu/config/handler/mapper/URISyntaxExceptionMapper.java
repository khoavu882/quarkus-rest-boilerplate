package com.github.kaivu.config.handler.mapper;

import com.github.kaivu.common.constant.AppHeaderConstant;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
public class URISyntaxExceptionMapper implements ExceptionMapper<URISyntaxException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(URISyntaxException ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);

        log.error(errorId, ex);

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
