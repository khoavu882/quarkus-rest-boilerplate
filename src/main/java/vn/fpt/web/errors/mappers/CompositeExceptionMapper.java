package vn.fpt.web.errors.mappers;

import io.smallrye.mutiny.CompositeException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import vn.fpt.constant.AppConstant;
import vn.fpt.constant.EntitiesConstant;
import vn.fpt.constant.ErrorsKeyConstant;
import vn.fpt.utils.ResourceBundleUtil;
import vn.fpt.web.errors.exceptions.EntityConflictException;
import vn.fpt.web.errors.exceptions.EntityNotFoundException;
import vn.fpt.web.errors.exceptions.NotAcceptableException;
import vn.fpt.web.errors.exceptions.PermissionDeniedException;
import vn.fpt.web.errors.exceptions.ServiceException;
import vn.fpt.web.errors.exceptions.UnauthorizedException;
import vn.fpt.web.errors.models.ErrorMessage;
import vn.fpt.web.errors.models.ErrorResponse;

import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
public class CompositeExceptionMapper implements ExceptionMapper<CompositeException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(CompositeException exs) {
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, exs);

        for (Throwable exception : exs.getCauses()) {
            // Handle each individual exception here
            if (exception instanceof UnauthorizedException) {

                // Remove cookie by re-set value to empty and set expiry to 0 (epoch timestamp)
                NewCookie removeAuthCookie = new NewCookie.Builder(HttpHeaders.AUTHORIZATION)
                        .httpOnly(true)
                        .path("/")
                        .value("")
                        .maxAge(0)
                        .build();

                return Response.status(Response.Status.UNAUTHORIZED)
                        .cookie(removeAuthCookie)
                        .build();
            } else if (exception instanceof PermissionDeniedException) {
                return Response.status(Response.Status.FORBIDDEN).build();
            } else if (exception instanceof EntityNotFoundException notFoundException) {
                ErrorMessage errorMessage = new ErrorMessage(
                        notFoundException.getEntityName() + "." + notFoundException.getErrorKey(),
                        notFoundException.getMessage());
                ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(errorResponse)
                        .build();
            } else if (exception instanceof EntityConflictException conflictException) {
                ErrorMessage errorMessage = new ErrorMessage(
                        conflictException.getEntityName() + "." + conflictException.getErrorKey(),
                        conflictException.getMessage());
                ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
                return Response.status(Response.Status.CONFLICT)
                        .entity(errorResponse)
                        .build();
            } else if (exception instanceof NotAcceptableException notAcceptableException) {
                ErrorMessage errorMessage = new ErrorMessage(
                        notAcceptableException.getEntityName() + "." + notAcceptableException.getErrorKey(),
                        notAcceptableException.getMessage());
                ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity(errorResponse)
                        .build();
            } else if (exception instanceof ServiceException serviceException) {
                // Handle other types of exceptions
                ErrorMessage errorMessage = new ErrorMessage(
                        serviceException.getEntityName() + "." + serviceException.getErrorKey(),
                        serviceException.getMessage());
                ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);

                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }
        }

        return getDefaultErrorResponse(errorId);
    }

    private Response getDefaultErrorResponse(String errorId) {

        String errorKey = EntitiesConstant.SYSTEM + "." + ErrorsKeyConstant.ERROR_NON_DEFINED;
        String defaultErrorMessage = ResourceBundleUtil.getKeyWithResourceBundle(
                AppConstant.I18N_ERROR, requestContext.getLanguage(), errorKey);

        ErrorMessage errorMessage = new ErrorMessage(errorKey, defaultErrorMessage);
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
