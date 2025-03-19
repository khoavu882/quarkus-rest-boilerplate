package com.github.kaivu.web.errors.mappers;

import com.github.kaivu.web.errors.exceptions.NotAcceptableException;
import com.github.kaivu.web.errors.exceptions.PermissionDeniedException;
import com.github.kaivu.web.errors.exceptions.ServiceException;
import com.github.kaivu.web.errors.exceptions.UnauthorizedException;
import com.github.kaivu.web.errors.models.ErrorMessage;
import com.github.kaivu.web.errors.models.ErrorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.NewCookie;
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
@ApplicationScoped
public class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    @Override
    public Response toResponse(ServiceException ex) {
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, ex);

        ErrorMessage errorMessage = new ErrorMessage(ex.getEntityName() + "." + ex.getErrorKey(), ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);

        if (ex.getClass().equals(UnauthorizedException.class)) {
            // Remove cookie by re-set value to empty and set expiry to 0 (epoch timestamp)
            NewCookie removeAuthCookie = new NewCookie.Builder("accessToken")
                    .httpOnly(true)
                    .path("/")
                    .value("")
                    .maxAge(0)
                    .build();

            return Response.status(Response.Status.UNAUTHORIZED)
                    .cookie(removeAuthCookie)
                    .build();
        } else if (ex.getClass().equals(PermissionDeniedException.class)) {

            return Response.status(Response.Status.FORBIDDEN).build();
        } else if (ex.getClass().equals(NotAcceptableException.class)) {

            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        } else {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }
}
