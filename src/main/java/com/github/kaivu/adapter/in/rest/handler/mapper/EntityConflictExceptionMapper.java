package com.github.kaivu.adapter.in.rest.handler.mapper;

import com.github.kaivu.adapter.in.rest.dto.response.ErrorMessage;
import com.github.kaivu.adapter.in.rest.dto.response.ErrorResponse;
import com.github.kaivu.application.exception.EntityConflictException;
import com.github.kaivu.common.constant.AppHeaderConstant;
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
public class EntityConflictExceptionMapper implements ExceptionMapper<EntityConflictException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(EntityConflictException ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);

        log.error(errorId, ex);

        ErrorMessage errorMessage = new ErrorMessage(ex.getEntityName() + "." + ex.getErrorKey(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);

        return Response.status(Response.Status.CONFLICT).entity(errorResponse).build();
    }
}
