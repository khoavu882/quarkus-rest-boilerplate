package com.github.kaivu.infrastructure.errors.mappers;

import com.github.kaivu.infrastructure.errors.exceptions.EntityNotFoundException;
import com.github.kaivu.infrastructure.errors.models.ErrorMessage;
import com.github.kaivu.infrastructure.errors.models.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
public class EntityNotFoundExceptionMapper implements ExceptionMapper<EntityNotFoundException> {

    @Override
    public Response toResponse(EntityNotFoundException ex) {
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, ex);

        ErrorMessage errorMessage = new ErrorMessage(ex.getEntityName() + "." + ex.getErrorKey(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);

        return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
    }
}
