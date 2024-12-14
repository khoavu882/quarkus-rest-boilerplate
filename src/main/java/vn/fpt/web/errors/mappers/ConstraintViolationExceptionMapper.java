package vn.fpt.web.errors.mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import vn.fpt.web.errors.models.ErrorMessage;
import vn.fpt.web.errors.models.ErrorResponse;

import java.util.List;
import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, ex);

        List<ErrorMessage> errorMessages = ex.getConstraintViolations().stream()
                .map(constraintViolation -> new ErrorMessage(
                        constraintViolation.getPropertyPath().toString(), null, constraintViolation.getMessage()))
                .toList();
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(errorId, errorMessages))
                .build();
    }
}
