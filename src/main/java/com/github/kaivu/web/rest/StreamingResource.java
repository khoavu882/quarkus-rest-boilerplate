package com.github.kaivu.web.rest;

import com.github.kaivu.annotations.MinioServer;
import com.github.kaivu.services.MinioService;
import com.github.kaivu.web.errors.ErrorsEnum;
import com.github.kaivu.web.errors.exceptions.ServiceException;
import com.github.kaivu.web.errors.models.ErrorResponse;
import com.google.common.net.HttpHeaders;
import io.minio.errors.MinioException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * REST controller for Streaming.
 */
@Slf4j
@Path("/stream")
@Tag(name = "Stream", description = "Streaming Resource")
@AllArgsConstructor
public class StreamingResource {

    @Inject
    @MinioServer("core")
    MinioService minioService;

    @Context
    Request request;

    @GET
    @Path("/{bucketName}/{objectName}")
    @APIResponse(responseCode = "200", description = "", content = @Content(mediaType = "video/mp4"))
    @APIResponse(
            responseCode = "500",
            description = "",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Response streamVideo(
            @Context ContainerRequestContext requestContext,
            @PathParam("bucketName") String bucketName,
            @PathParam("objectName") String objectName,
            @HeaderParam(HttpHeaders.RANGE) String rangeHeader) {

        try {

            // Get the size of the object (video)
            long fileSize = minioService.getSize(bucketName, objectName);
            long startByte = 0;
            long endByte = fileSize - 1;

            // Parse the range header
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                startByte = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    endByte = Long.parseLong(ranges[1]);
                }
            }

            // Set the content range header
            String contentRange = String.format("bytes %d-%d/%d", startByte, endByte, fileSize);

            // Set the response status and headers
            Response.ResponseBuilder responseBuilder = Response.status(Response.Status.PARTIAL_CONTENT)
                    .header("Content-Disposition", "inline; filename=\"" + objectName + "\"")
                    .header("Content-Type", MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Range", contentRange)
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Length", endByte - startByte + 1);

            // Get InputStream for the video object from MinIO
            InputStream inputStream =
                    minioService.getObject(bucketName, objectName, startByte, endByte - startByte + 1);

            // Stream the requested byte range
            return responseBuilder.entity(inputStream).build();

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException ex) {
            log.error(ex.getMessage(), ex);
            throw new ServiceException(
                    ErrorsEnum.SYSTEM_INTERNAL_SERVER_ERROR.withLocale(requestContext.getLanguage()));
        }
    }
}
