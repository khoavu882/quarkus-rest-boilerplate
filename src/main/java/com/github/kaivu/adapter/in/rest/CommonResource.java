package com.github.kaivu.adapter.in.rest;

import com.github.kaivu.configuration.handler.ErrorResponse;
import com.github.kaivu.common.constant.AppConstant;
import com.github.kaivu.common.utils.ResourceBundleUtil;
import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Locale;
import java.util.Map;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/15/25
 * Time: 1:17 AM
 * Description:
 * REST controller Common API.
 */
@Slf4j
@Path("/common")
@Tag(name = "Common API", description = "Common API public for Web applications filtering")
public class CommonResource {

    @GET
    @Path("/bundles/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(operationId = "getBundleMessages", summary = "Get bundle resource messages with string context")
    @APIResponses(
            value = {
                @APIResponse(responseCode = "200"),
                @APIResponse(
                        responseCode = "500",
                        description = "",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON,
                                        schema = @Schema(implementation = ErrorResponse.class)))
            })
    public Response getBundleMessages(@PathParam("name") String name, @Context ContainerRequestContext requestContext) {

        Map<Locale, Map<String, String>> messages = ResourceBundleUtil.getAllMessages(
                AppConstant.PATH_I18N + "/" + name, requestContext.getAcceptableLanguages());
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<Locale, Map<String, String>> entry : messages.entrySet()) {
            JsonObject localeMessages = new JsonObject();
            for (Map.Entry<String, String> messageEntry : entry.getValue().entrySet()) {
                localeMessages.put(messageEntry.getKey(), messageEntry.getValue());
            }
            jsonObject.put(entry.getKey().toString(), localeMessages);
        }

        return Response.ok().entity(jsonObject.encode()).build();
    }
}
