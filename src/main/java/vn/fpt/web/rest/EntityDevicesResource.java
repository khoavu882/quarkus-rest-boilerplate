package vn.fpt.web.rest;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;
import vn.fpt.models.EntityDevice;
import vn.fpt.services.EntityDevicesService;
import vn.fpt.services.dto.CreateEntityDTO;
import vn.fpt.web.errors.models.ErrorResponse;

@Slf4j
@Path("/api/entity-devices")
@Tag(name = "Entity Devices", description = "Entity Devices Resource")
public class EntityDevicesResource {

    @Inject
    EntityDevicesService entityDevicesService;

    @POST
    @Operation(operationId = "createEntityDevice", summary = "Create a new Entity Device")
    @APIResponse(responseCode = "201", description = "", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(
            responseCode = "500",
            description = "",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<RestResponse<EntityDevice>> entity(
            @RequestBody(
                            description = "Entity Device to create",
                            content = @Content(schema = @Schema(implementation = CreateEntityDTO.class)))
                    @Valid
                    CreateEntityDTO dto) {

        return entityDevicesService.create(dto).map(device -> RestResponse.status(RestResponse.Status.CREATED));
    }
}
