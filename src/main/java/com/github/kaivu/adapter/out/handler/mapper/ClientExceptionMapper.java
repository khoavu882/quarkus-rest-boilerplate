package com.github.kaivu.adapter.out.handler.mapper;

import com.github.kaivu.configuration.handler.ErrorMessage;
import com.github.kaivu.configuration.handler.ErrorResponse;
import com.github.kaivu.adapter.out.exception.ClientException;
import com.github.kaivu.adapter.out.exception.DemoClientException;
import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
public class ClientExceptionMapper implements ExceptionMapper<ClientException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(ClientException ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);

        log.error(errorId, ex);

        if (Boolean.TRUE.equals(ex.getPassThrough())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ex.getClientMessage())
                    .build();
        }
        String message = ResourceBundleUtil.getKeyWithResourceBundleOrThrow(
                AppConstant.I18N_ERROR,
                requestContext.getLanguage(),
                ex.getErrorsEnum().getFullKey());

        ErrorResponse errorResponse = getErrorResponse(ex, message, errorId);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }

    private static @NotNull ErrorResponse getErrorResponse(ClientException ex, String message, String errorId) {
        String formattedMessage = ex instanceof DemoClientException demoClientException
                ? String.format(
                        message,
                        ex.getEntityName(),
                        demoClientException.getResponse().getStatus())
                : message;

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorKey(ex.getErrorsEnum().getFullKey());
        errorMessage.setMessage(formattedMessage);

        return new ErrorResponse(errorId, errorMessage);
    }
}
