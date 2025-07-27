package com.github.kaivu.adapter.in.rest;

import com.github.kaivu.adapter.in.rest.dto.request.CreateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.request.EntityDeviceFilters;
import com.github.kaivu.adapter.in.rest.dto.request.UpdateEntityDTO;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceDetailsVM;
import com.github.kaivu.adapter.in.rest.dto.vm.EntityDeviceVM;
import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.application.usecase.EntityDeviceUseCase;
import com.github.kaivu.config.handler.ErrorResponse;
import com.github.kaivu.domain.EntityDevice;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Path("/api/entity-devices")
@SecurityRequirement(name = "UserToken")
@Tag(name = "Entity Devices", description = "Entity Devices Resource")
public class EntityDevicesResource {

    @Inject
    EntityDeviceUseCase entityDeviceUseCase;

    @POST
    @Operation(operationId = "createEntityDevice", summary = "Create a new Entity Device")
    @APIResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(
            responseCode = "500",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<RestResponse<EntityDevice>> create(
            @Context UriInfo uriInfo,
            @RequestBody(
                            description = "Entity Device to create",
                            content = @Content(schema = @Schema(implementation = CreateEntityDTO.class)))
                    @Valid
                    CreateEntityDTO dto) {

        return entityDeviceUseCase.create(dto).map(device -> {
            URI location = uriInfo.getAbsolutePathBuilder()
                    .path(device.getId().toString())
                    .build();
            return RestResponse.created(location);
        });
    }

    @PUT
    @Path("/{id}")
    @Operation(operationId = "updateEntityDevice", summary = "Update a Entity Device by ID")
    @APIResponse(
            responseCode = "200",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EntityDeviceVM.class, type = SchemaType.OBJECT)))
    @APIResponse(
            responseCode = "500",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<RestResponse<EntityDeviceVM>> update(
            @PathParam("id") UUID id,
            @RequestBody(
                            description = "Entity Device to update",
                            content = @Content(schema = @Schema(implementation = UpdateEntityDTO.class)))
                    @Valid
                    UpdateEntityDTO dto) {

        return entityDeviceUseCase.update(id, dto).map(RestResponse::ok);
    }

    @GET
    @Path("/{id}")
    @Operation(operationId = "getEntityDeviceDetails", summary = "Get details an Entity Device by ID")
    @APIResponse(
            responseCode = "200",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EntityDeviceDetailsVM.class)))
    @APIResponse(
            responseCode = "500",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<RestResponse<EntityDeviceDetailsVM>> details(@PathParam("id") UUID id) {
        return entityDeviceUseCase.details(id).map(RestResponse::ok);
    }

    @GET
    @Operation(operationId = "pageableEntityDeviceDetails", summary = "Pageable Entity Devices by filters")
    @APIResponse(
            responseCode = "200",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = EntityDeviceVM.class, type = SchemaType.ARRAY)))
    @APIResponse(
            responseCode = "500",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<RestResponse<PageResponse<EntityDeviceVM>>> pageable(@BeanParam EntityDeviceFilters filters) {
        return entityDeviceUseCase.pageable(filters).map(RestResponse::ok);
    }

    @DELETE
    @Path("/{id}")
    @Operation(operationId = "deleteEntityDevice", summary = "Delete an Entity Device by ID")
    @APIResponse(responseCode = "204")
    @APIResponse(
            responseCode = "500",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<RestResponse<Void>> delete(@PathParam("id") UUID id) {
        return entityDeviceUseCase.delete(id).map(ignore -> RestResponse.noContent());
    }
}
