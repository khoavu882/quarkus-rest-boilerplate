package com.github.kaivu.config.handler;

import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.common.constant.EntitiesConstant;
import com.github.kaivu.common.constant.ErrorsKeyConstant;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Provider
@ApplicationScoped
@RegisterForReflection
public class ErrorsHandler implements ExceptionMapper<Throwable> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(Throwable ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);
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
