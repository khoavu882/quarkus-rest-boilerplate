package vn.fpt.services.client;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import vn.fpt.constant.AppConstant;
import vn.fpt.web.errors.ErrorsEnum;
import vn.fpt.web.errors.exceptions.ServiceException;

@RequestScoped
@RegisterRestClient(configKey = AppConstant.DEMO_CLIENT)
public interface DemoClientService {

    /**
     * API get User Permission async.
     *
     * @return DmcUserInfo.
     */
    @GET
    @Path("/common/bundles/test")
    Uni<Response> demo();

    @ClientExceptionMapper
    static RuntimeException toException(Response response) {

        if (Response.Status.BAD_REQUEST.getStatusCode() <= response.getStatus()
                && response.getStatus() <= Response.Status.NETWORK_AUTHENTICATION_REQUIRED.getStatusCode()) {
            return new ServiceException(
                    ErrorsEnum.SYSTEM_CLIENT_BAD_REQUEST.withLocale(response.getLanguage(), response.getStatus()));
        }
        return null;
    }
}
