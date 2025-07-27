package com.github.kaivu.config.handler.mapper;

import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.config.handler.ErrorMessage;
import com.github.kaivu.config.handler.ErrorResponse;
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
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(EntityNotFoundException ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);

        log.error(errorId, ex);

        ErrorMessage errorMessage = new ErrorMessage(ex.getEntityName() + "." + ex.getErrorKey(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);

        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
    }
}
