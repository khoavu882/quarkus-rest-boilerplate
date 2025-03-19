package com.github.kaivu.web.errors.mappers;

import com.github.kaivu.constant.AppConstant;
import com.github.kaivu.utils.ResourceBundleUtil;
import com.github.kaivu.web.errors.exceptions.ClientException;
import com.github.kaivu.web.errors.models.ErrorMessage;
import com.github.kaivu.web.errors.models.ErrorResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

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
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, ex);

        if (ex.getPassThrough()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ex.getClientMessage())
                    .build();
        }
        String message = ResourceBundleUtil.getKeyWithResourceBundleOrThrow(
                AppConstant.I18N_ERROR,
                requestContext.getLanguage(),
                ex.getErrorsEnum().getFullKey());

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setErrorKey(ex.getErrorsEnum().getFullKey());
        errorMessage.setMessage(message);

        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
