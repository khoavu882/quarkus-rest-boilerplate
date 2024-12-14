package vn.fpt.web.errors.mappers;

import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 10:00 AM
 */
@Slf4j
@Provider
public class NotAllowedExceptionMapper implements ExceptionMapper<NotAllowedException> {

    @Override
    public Response toResponse(NotAllowedException ex) {
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, ex);

        return Response.status(Response.Status.METHOD_NOT_ALLOWED).build();
    }
}
