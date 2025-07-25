package com.github.kaivu.adapter.out.api;

import com.github.kaivu.adapter.out.exception.DemoClientException;
import com.github.kaivu.adapter.out.handler.ClientErrorsEnum;
import com.github.kaivu.common.constant.ClientConfigKeyConstant;
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
            case 401 -> // Response.Status.UNAUTHORIZED
            new DemoClientException(ClientErrorsEnum.DEMO_REST_UNAUTHORIZED, response);
            case 403 -> // Response.Status.FORBIDDEN
            new DemoClientException(ClientErrorsEnum.DEMO_REST_PERMISSION_DENIED, response);
            case 409 -> // Response.Status.CONFLICT
            new DemoClientException(ClientErrorsEnum.DEMO_REST_CONFLICT, response);
            default -> {
                if (400 <= status && status < 500) {
                    yield new DemoClientException(ClientErrorsEnum.DEMO_REST_CLIENT_BAD_REQUEST, response);
                }
                if (500 <= status && status <= 511) {
                    yield new DemoClientException(ClientErrorsEnum.DEMO_REST_CLIENT_INTERNAL_SERVER_ERROR, response);
                }
                yield null;
            }
        };
    }
}
