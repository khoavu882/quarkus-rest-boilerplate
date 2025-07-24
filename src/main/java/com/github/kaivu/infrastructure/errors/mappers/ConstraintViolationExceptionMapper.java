package com.github.kaivu.infrastructure.errors.mappers;

import com.github.kaivu.domain.constant.AppHeaderConstant;
import com.github.kaivu.infrastructure.errors.models.ErrorMessage;
import com.github.kaivu.infrastructure.errors.models.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);
        log.error("Validation error [{}]:", errorId, ex);

        List<ErrorMessage> errorMessages = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String propertyPath = violation.getPropertyPath().toString();
                    String field = propertyPath.substring(propertyPath.indexOf('.') + 1);
                    return new ErrorMessage(field, violation.getMessageTemplate(), violation.getMessage());
                })
                .toList();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(errorId, errorMessages))
                .build();
    }
}
