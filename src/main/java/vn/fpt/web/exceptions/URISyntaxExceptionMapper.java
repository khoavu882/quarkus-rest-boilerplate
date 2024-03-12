package vn.fpt.web.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@Provider
public class URISyntaxExceptionMapper implements ExceptionMapper<URISyntaxException> {

    @Override
    public Response toResponse(URISyntaxException ex) {
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, ex.getMessage());

        ErrorResponse.ErrorMessage errorMessage = new ErrorResponse.ErrorMessage(ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .build();
    }

}