package com.github.kaivu.adapter.in.rest;

import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.configuration.handler.ErrorResponse;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import com.github.kaivu.configuration.minio.MinioManager;
import com.github.kaivu.configuration.minio.MinioProfile;
import com.github.kaivu.configuration.minio.MinioProfileType;
import com.google.common.net.HttpHeaders;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST controller for Streaming using MinioManager with annotation profile selection.
 * Uses @MinioProfile at injection point to specify which MinIO profile to use.
 */
@Slf4j
@Path("/stream")
@Tag(name = "Stream", description = "Streaming Resource")
public class StreamingResource {

    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d*)");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "mp4", "video/mp4",
            "webm", "video/webm",
            "mov", "video/quicktime",
            "avi", "video/x-msvideo",
            "mkv", "video/x-matroska");

    @Inject
    @MinioProfile(MinioProfileType.MEDIA)
    MinioManager minioManager;

    @GET
    @Path("/{bucketName}/{objectName}")
    @APIResponse(responseCode = "200", description = "Stream video content")
    @APIResponse(responseCode = "206", description = "Partial content")
    @APIResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    public Uni<Response> streamVideo(
            @Context ContainerRequestContext requestContext,
            @PathParam("bucketName") String bucketName,
            @PathParam("objectName") String objectName,
            @HeaderParam(HttpHeaders.RANGE) String rangeHeader) {

        return minioManager
                .getObjectSize(bucketName, objectName)
                .onItem()
                .transformToUni(fileSize -> {
                    // Parse range header
                    RangeInfo rangeInfo = parseRangeHeader(rangeHeader, fileSize);
                    long startByte = rangeInfo.startByte;
                    long endByte = rangeInfo.endByte;

                    String contentType = determineContentType(objectName);
                    String contentRange = String.format("bytes %d-%d/%d", startByte, endByte, fileSize);
                    long contentLength = endByte - startByte + 1;

                    return minioManager
                            .getObject(bucketName, objectName, startByte, contentLength)
                            .onItem()
                            .transform(inputStream -> {
                                // Set the response status and headers
                                return Response.status(Response.Status.PARTIAL_CONTENT)
                                        .header("Content-Disposition", "inline; filename=\"" + objectName + "\"")
                                        .header("Content-Type", contentType)
                                        .header("Content-Range", contentRange)
                                        .header("Accept-Ranges", "bytes")
                                        .header("Content-Length", contentLength)
                                        // Add caching headers
                                        .header("Cache-Control", "public, max-age=86400")
                                        .header("ETag", "\"" + objectName.hashCode() + "\"")
                                        .entity(inputStream)
                                        .build();
                            });
                })
                .onFailure()
                .transform(ex -> {
                    log.error("Error while streaming: {}", ex.getMessage(), ex);
                    return new ServiceException(
                            ErrorsEnum.SYSTEM_INTERNAL_SERVER_ERROR.withLocale(requestContext.getLanguage()));
                });
    }

    private RangeInfo parseRangeHeader(String rangeHeader, long fileSize) {
        long startByte = 0;
        long endByte = fileSize - 1;

        if (rangeHeader != null) {
            Matcher matcher = RANGE_PATTERN.matcher(rangeHeader);
            if (matcher.matches()) {
                startByte = Long.parseLong(matcher.group(1));
                String endByteStr = matcher.group(2);
                if (endByteStr != null && !endByteStr.isEmpty()) {
                    endByte = Long.parseLong(endByteStr);
                }

                // Validate range
                if (startByte >= fileSize) {
                    startByte = 0;
                }
                if (endByte >= fileSize) {
                    endByte = fileSize - 1;
                }
            }
        }

        return new RangeInfo(startByte, endByte);
    }

    private String determineContentType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex + 1).toLowerCase();
            return CONTENT_TYPES.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM);
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private record RangeInfo(long startByte, long endByte) {}
}
