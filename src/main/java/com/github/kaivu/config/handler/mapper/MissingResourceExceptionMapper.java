package com.github.kaivu.config.handler.mapper;

import com.github.kaivu.common.constant.AppHeaderConstant;
import com.github.kaivu.config.handler.ErrorMessage;
import com.github.kaivu.config.handler.ErrorResponse;
import com.github.kaivu.config.handler.ErrorsEnum;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.MissingResourceException;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
@RequestScoped
public class MissingResourceExceptionMapper implements ExceptionMapper<MissingResourceException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(MissingResourceException ex) {
        String errorId = requestContext.getHeaderString(AppHeaderConstant.TRACE_ID);

        log.error(errorId, ex);

        // Resolved via ErrorsEnum's cached, fallback-safe getMessage — never via a raw
        // ResourceBundle lookup here, since this mapper's whole job is handling a bundle
        // lookup failure; it must not be able to throw the same exception while doing so.
        ErrorsEnum errorsEnum = ErrorsEnum.SYSTEM_BUNDLE_DOES_NOT_EXIST;
        String message = errorsEnum.getMessage(requestContext.getLanguage());

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setKey(errorsEnum.getFullKey());
        errorMessage.setMessage(message);

        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
