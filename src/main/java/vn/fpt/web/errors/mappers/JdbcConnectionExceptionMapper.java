package vn.fpt.web.errors.mappers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.JDBCConnectionException;
import vn.fpt.constant.AppConstant;
import vn.fpt.constant.EntitiesConstant;
import vn.fpt.constant.ErrorsKeyConstant;
import vn.fpt.utils.ResourceBundleUtil;
import vn.fpt.web.errors.models.ErrorMessage;
import vn.fpt.web.errors.models.ErrorResponse;

import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 2/12/24
 * Time: 9:04 AM
 */
@Slf4j
@Provider
@RequestScoped
public class JdbcConnectionExceptionMapper implements ExceptionMapper<JDBCConnectionException> {

    @Context
    ContainerRequestContext requestContext;

    @Override
    public Response toResponse(JDBCConnectionException ex) {
        String errorId = UUID.randomUUID().toString();

        log.error(errorId, ex);

        String key = EntitiesConstant.SYSTEM + "." + ErrorsKeyConstant.DATABASE_CONNECTION_FAILED;
        String message =
                ResourceBundleUtil.getKeyWithResourceBundle(AppConstant.I18N_ERROR, requestContext.getLanguage(), key);

        ErrorMessage errorMessage = new ErrorMessage(key, message);
        ErrorResponse errorResponse = new ErrorResponse(errorId, errorMessage);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
    }
}
