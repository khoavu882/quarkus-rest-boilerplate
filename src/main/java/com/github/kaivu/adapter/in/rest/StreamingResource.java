package com.github.kaivu.adapter.in.rest;

import com.github.kaivu.adapter.in.rest.dto.vm.RangeInfo;
import com.github.kaivu.adapter.in.rest.dto.vm.StreamingResponse;
import com.github.kaivu.application.usecase.MediaStreamingService;
import com.github.kaivu.configuration.handler.ErrorResponse;
import com.google.common.net.HttpHeaders;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST controller for Streaming using MinioManager with annotation profile selection.
 * Uses @MinioProfile at injection point to specify which MinIO profile to use.
 */
@Slf4j
@Path("/stream")
@Tag(name = "Stream", description = "Streaming Resource")
@RolesAllowed({"USER", "ADMIN"})
public class StreamingResource {

    private final MediaStreamingService mediaStreamingService;

    @Inject
    public StreamingResource(MediaStreamingService mediaStreamingService) {
        this.mediaStreamingService = mediaStreamingService;
    }

    @GET
    @Path("/{bucketName}/{objectName}")
    @APIResponse(responseCode = "206", description = "Partial content")
    @APIResponse(responseCode = "404", description = "Media file not found")
    @APIResponse(responseCode = "416", description = "Range not satisfiable")
    @APIResponse(
            responseCode = "500",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    @Counted(value = "streaming_requests_total", description = "Total streaming requests")
    @Timed(value = "streaming_request_duration", description = "Streaming request duration")
    public Uni<Response> streamVideo(
            @PathParam("bucketName") @Pattern(regexp = "^[a-z0-9][a-z0-9.-]*[a-z0-9]$") String bucketName,
            @PathParam("objectName") @NotBlank @Size(max = 255) String objectName,
            @HeaderParam(HttpHeaders.RANGE) String rangeHeader) {

        return mediaStreamingService
                .prepareStreamingResponse(bucketName, objectName, rangeHeader)
                .map(this::buildStreamingResponse);
    }

    private Response buildStreamingResponse(StreamingResponse streamingResponse) {
        RangeInfo range = streamingResponse.rangeInfo();
        long fileSize = streamingResponse.fileSize();

        return Response.status(Response.Status.PARTIAL_CONTENT)
                .header("Content-Type", streamingResponse.contentType())
                .header("Content-Range", String.format("bytes %d-%d/%d", range.startByte(), range.endByte(), fileSize))
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", range.endByte() - range.startByte() + 1)
                .header("Cache-Control", "public, max-age=86400")
                .header("ETag", "\"" + streamingResponse.etag() + "\"")
                .entity(streamingResponse.inputStream())
                .build();
    }
}
