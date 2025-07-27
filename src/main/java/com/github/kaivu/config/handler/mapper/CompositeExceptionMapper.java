package com.github.kaivu.config.handler.mapper;

import com.github.kaivu.application.exception.EntityConflictException;
import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.application.exception.NotAcceptableException;
import com.github.kaivu.application.exception.PermissionDeniedException;
import com.github.kaivu.application.exception.UnauthorizedException;
import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.common.constant.EntitiesConstant;
import com.github.kaivu.common.constant.ErrorsKeyConstant;
import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import com.github.kaivu.config.handler.ErrorMessage;
import com.github.kaivu.config.handler.ErrorResponse;
import io.smallrye.mutiny.CompositeException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Provider
@ApplicationScoped
public class CompositeExceptionMapper implements ExceptionMapper<CompositeException> {

    @Context
    ContainerRequestContext requestContext;

    private final Map<Class<? extends Throwable>, Function<Throwable, Response>> exceptionHandlers = Map.of(
            UnauthorizedException.class, ex -> handleUnauthorized(),
            PermissionDeniedException.class,
                    ex -> Response.status(Response.Status.FORBIDDEN).build(),
            EntityNotFoundException.class, this::handleNotFound,
            EntityConflictException.class, this::handleConflict,
            NotAcceptableException.class, this::handleNotAcceptable,
            ServiceException.class, this::handleServiceException);

    @Override
    public Response toResponse(CompositeException exs) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);
        log.error(errorId, exs);

        return exs.getCauses().stream()
                .map(ex -> exceptionHandlers.entrySet().stream()
                        .filter(entry -> entry.getKey().isInstance(ex))
                        .findFirst()
                        .map(entry -> entry.getValue().apply(ex))
                        .orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> getDefaultErrorResponse(errorId));
    }

    private Response handleUnauthorized() {
        NewCookie removeAuthCookie = new NewCookie.Builder(HttpHeaders.AUTHORIZATION)
                .httpOnly(true)
                .path("/")
                .value("")
                .maxAge(0)
                .build();
        return Response.status(Response.Status.UNAUTHORIZED)
                .cookie(removeAuthCookie)
                .build();
    }

    private Response handleNotFound(Throwable ex) {
        EntityNotFoundException exception = (EntityNotFoundException) ex;
        return buildErrorResponse(
                Response.Status.NOT_FOUND, exception.getEntityName(), exception.getErrorKey(), exception.getMessage());
    }

    private Response handleConflict(Throwable ex) {
        EntityConflictException exception = (EntityConflictException) ex;
        return buildErrorResponse(
                Response.Status.CONFLICT, exception.getEntityName(), exception.getErrorKey(), exception.getMessage());
    }

    private Response handleNotAcceptable(Throwable ex) {
        NotAcceptableException exception = (NotAcceptableException) ex;
        return buildErrorResponse(
                Response.Status.NOT_ACCEPTABLE,
                exception.getEntityName(),
                exception.getErrorKey(),
                exception.getMessage());
    }

    private Response handleServiceException(Throwable ex) {
        ServiceException exception = (ServiceException) ex;
        return buildErrorResponse(
                Response.Status.BAD_REQUEST,
                exception.getEntityName(),
                exception.getErrorKey(),
                exception.getMessage());
    }

    private Response buildErrorResponse(Response.Status status, String entityName, String errorKey, String message) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);
        ErrorMessage errorMessage = new ErrorMessage(entityName + "." + errorKey, message);
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
        return Response.status(status).entity(errorResponse).build();
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
