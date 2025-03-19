package com.github.kaivu.services.client;

import com.github.kaivu.constant.ClientConfigKeyConstant;
import com.github.kaivu.web.errors.ClientErrorsEnum;
import com.github.kaivu.web.errors.exceptions.DemoClientException;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RequestScoped
@RegisterRestClient(configKey = ClientConfigKeyConstant.DEMO_CLIENT)
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

        int status = response.getStatus();
        return switch (status) {
            case 400 -> // Response.Status.BAD_REQUEST
            new DemoClientException(response);
            case 401 -> // Response.Status.UNAUTHORIZED
            new DemoClientException(ClientErrorsEnum.DEMO_REST_UNAUTHORIZED, response);
            case 403 -> // Response.Status.FORBIDDEN
            new DemoClientException(ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED, response);
            case 409 -> // Response.Status.CONFLICT
            new DemoClientException(ClientErrorsEnum.DEMO_REST_CONFLICT, response);
            default -> {
                if (400 <= status && status <= 511) {
                    yield new DemoClientException(response);
                }
                yield null;
            }
        };
    }
}
