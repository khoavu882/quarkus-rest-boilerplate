package com.github.kaivu.web.errors;

import com.github.kaivu.constant.AppConstant;
import com.github.kaivu.constant.EntitiesConstant;
import com.github.kaivu.constant.ErrorsKeyConstant;
import com.github.kaivu.utils.ResourceBundleUtil;
import com.github.kaivu.web.errors.models.ErrorMessage;
import com.github.kaivu.web.errors.models.ErrorResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Provider
@ApplicationScoped
@RegisterForReflection
public class ErrorsHandler implements ExceptionMapper<Throwable> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(Throwable ex) {
        String errorId = UUID.randomUUID().toString();
        String errorKey = EntitiesConstant.SYSTEM + "." + ErrorsKeyConstant.ERROR_NON_DEFINED;

        log.error("Error ID: {} - Exception: {}", errorId, ex.getMessage(), ex);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(buildErrorResponse(errorId, errorKey))
                .build();
    }

    private ErrorResponse buildErrorResponse(String errorId, String errorKey) {
        String defaultErrorMessage = ResourceBundleUtil.getKeyWithResourceBundle(
                AppConstant.I18N_ERROR, requestContext.getLanguage(), errorKey);
        return new ErrorResponse(errorId, new ErrorMessage(errorKey, defaultErrorMessage));
    }
}
