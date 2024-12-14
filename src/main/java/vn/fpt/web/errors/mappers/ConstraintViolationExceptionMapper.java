package vn.fpt.web.errors.mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import vn.fpt.constant.EntitiesConstant;
import vn.fpt.constant.ErrorsKeyConstant;
import vn.fpt.web.errors.models.ErrorMessage;
import vn.fpt.web.errors.models.ErrorResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final String ERROR_PREFIX = EntitiesConstant.FORM + "." + ErrorsKeyConstant.CONSTRAINT_VIOLATION;

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("Validation error [{}]:", errorId, ex);

        List<ErrorMessage> errorMessages = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorMessage(
                        violation.getPropertyPath().toString(),
                        ERROR_PREFIX,
                        violation.getMessage()))
                .toList();

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(errorId, errorMessages))
                .build();
    }
}
