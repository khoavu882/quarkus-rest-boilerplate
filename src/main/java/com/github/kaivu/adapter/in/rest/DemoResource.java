package com.github.kaivu.adapter.in.rest;

import com.github.kaivu.configuration.handler.ErrorResponse;
import com.github.kaivu.adapter.out.api.DemoClientService;
import com.github.kaivu.application.exception.EntityNotFoundException;
import com.github.kaivu.application.exception.NotAcceptableException;
import com.github.kaivu.application.exception.PermissionDeniedException;
import com.github.kaivu.application.exception.UnauthorizedException;
import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.configuration.annotations.ValidEnumValue;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import com.github.kaivu.domain.enumeration.ActionStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;

import java.io.Serializable;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 3/12/24
 * Time: 10:32 AM
 * REST controller Common API.
 */
@Slf4j
@Path("/demo")
@SecurityRequirement(name = "UserToken")
@Tag(name = "Demo API", description = "Demo API Web applications")
public class DemoResource {

    @RestClient
    DemoClientService demoClient;

    @GET
    @Path("/rest/client")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "demoRestException", summary = "Demo Rest Client throw Exceptions.")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = JsonObject.class))),
                @APIResponse(
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class))),
                @APIResponse(
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public Uni<RestResponse<Void>> demoRestException() {

        return demoClient.demo().replaceWith(RestResponse::ok);
    }

    @GET
    @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "demoException", summary = "Demo throw Exceptions.")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = JsonObject.class))),
                @APIResponse(
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class))),
                @APIResponse(
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public Uni<RestResponse<Void>> demoException(
            @PathParam("code") Integer code, @Context ContainerRequestContext requestContext) {

        if (code == null) {
            return Uni.createFrom().item(RestResponse.ok());
        } else if (code.equals(Response.Status.UNAUTHORIZED.getStatusCode())) {
            throw new UnauthorizedException(ErrorsEnum.AUTH_UNAUTHORIZED);
        } else if (code.equals(Response.Status.FORBIDDEN.getStatusCode())) {
            throw new PermissionDeniedException(ErrorsEnum.AUTH_NO_ACCESS);
        } else if (code.equals(Response.Status.BAD_REQUEST.getStatusCode())) {
            throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_TIME_RANGE.withLocale(requestContext.getLanguage()));
        } else if (code.equals(Response.Status.NOT_FOUND.getStatusCode())) {
            throw new EntityNotFoundException(
                    ErrorsEnum.USER_NOT_FOUND.withLocale(requestContext.getLanguage(), "khoavu882@gmail.com"));
        } else if (code.equals(Response.Status.NOT_ACCEPTABLE.getStatusCode())) {
            throw new NotAcceptableException(
                    ErrorsEnum.SYSTEM_BUNDLE_DOES_NOT_EXIST.withLocale(requestContext.getLanguage()));
        } else {
            throw new InternalServerErrorException();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "demoPostException", summary = "Demo Post throw Exceptions.")
    @APIResponses(
            value = {
                @APIResponse(
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = JsonObject.class))),
                @APIResponse(
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class))),
                @APIResponse(
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public Uni<RestResponse<DemoRequest>> demoPostException(@RequestBody @Valid DemoRequest request) {
        return Uni.createFrom().item(RestResponse.ok(request));
    }

    @Getter
    @Setter
    @ToString
    public static class DemoRequest implements Serializable {

        @NotBlank(message = "user.name.invalid")
        @Size(max = 255, min = 4, message = "user.size.invalid")
        private String name;

        @Email(message = "user.email.invalid")
        private String email;

        @ValidEnumValue(enumClass = ActionStatus.class, message = "user.status.invalid")
        private String status;

        @Valid
        @NotNull(message = "user.metadata.invalid")
        private Metadata metadata;

        @Getter
        @Setter
        @ToString
        public static class Metadata implements Serializable {

            @NotNull(message = "user.metadata.key.invalid")
            private String key;

            @NotNull(message = "user.metadata.value.invalid")
            private String value;
        }
    }
}
