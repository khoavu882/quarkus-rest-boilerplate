package com.github.kaivu.application.usecase;

import com.github.kaivu.adapter.in.rest.dto.vm.RangeInfo;
import com.github.kaivu.adapter.in.rest.dto.vm.StreamingResponse;
import com.github.kaivu.application.port.IMediaFileRepository;
import com.github.kaivu.common.context.LanguageContext;
import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.config.handler.ErrorsEnum;
import com.github.kaivu.config.minio.MinioManager;
import com.github.kaivu.config.minio.MinioProfile;
import com.github.kaivu.config.minio.MinioProfileType;
import com.github.kaivu.domain.MediaFile;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 7/27/25
 * Time: 2:28 AM
 */
@ApplicationScoped
public class MediaStreamingService {

    private final IMediaFileRepository mediaFileRepository;
    private final MinioManager minioManager;
    private final MeterRegistry meterRegistry;
    private final LanguageContext languageContext;

    private Counter streamRequestCounter;
    private Timer streamDurationTimer;

    @Inject
    public MediaStreamingService(
            IMediaFileRepository mediaFileRepository,
            @MinioProfile(MinioProfileType.MEDIA) MinioManager minioManager,
            MeterRegistry meterRegistry,
            LanguageContext languageContext) {
        this.mediaFileRepository = mediaFileRepository;
        this.minioManager = minioManager;
        this.meterRegistry = meterRegistry;
        this.languageContext = languageContext;
    }

    @PostConstruct
    void initMetrics() {
        streamRequestCounter = Counter.builder("media.stream.requests")
                .description("Number of streaming requests")
                .register(meterRegistry);
        streamDurationTimer = Timer.builder("media.stream.duration")
                .description("Stream request duration")
                .register(meterRegistry);
    }

    public Uni<StreamingResponse> prepareStreamingResponse(String bucketName, String objectName, String rangeHeader) {
        Timer.Sample sample = Timer.start(meterRegistry);
        streamRequestCounter.increment();

        return mediaFileRepository
                .findByBucketAndObject(bucketName, objectName)
                .chain(optionalMediaFile -> {
                    if (optionalMediaFile.isEmpty()) {
                        // Use current language for localized error messages
                        ServiceException exception = new ServiceException(
                                ErrorsEnum.SYSTEM_CLIENT_BAD_REQUEST.withLocale(languageContext.getCurrentLocale()));
                        // You can now use currentLanguage for localization if your ServiceException supports it
                        return Uni.createFrom().failure(exception);
                    }

                    MediaFile mediaFile = optionalMediaFile.get();
                    RangeInfo rangeInfo = parseRangeHeader(rangeHeader, mediaFile.getFileSize());
                    return createStreamingResponse(mediaFile, rangeInfo);
                })
                .onTermination()
                .invoke(() -> sample.stop(streamDurationTimer));
    }

    private Uni<StreamingResponse> createStreamingResponse(MediaFile mediaFile, RangeInfo rangeInfo) {
        long contentLength = rangeInfo.endByte() - rangeInfo.startByte() + 1;

        return minioManager
                .getObject(mediaFile.getBucketName(), mediaFile.getObjectName(), rangeInfo.startByte(), contentLength)
                .map(inputStream -> new StreamingResponse(
                        rangeInfo,
                        mediaFile.getFileSize(),
                        mediaFile.getContentType(),
                        mediaFile.getEtag(),
                        inputStream));
    }

    private RangeInfo parseRangeHeader(String rangeHeader, Long fileSize) {
        if (rangeHeader == null || rangeHeader.isBlank()) {
            return new RangeInfo(0, fileSize - 1);
        }

        // Parse "bytes=start-end" format
        if (rangeHeader.startsWith("bytes=")) {
            String range = rangeHeader.substring(6);
            String[] parts = range.split("-");

            long startByte = 0;
            long endByte = fileSize - 1;

            if (parts.length > 0 && !parts[0].isEmpty()) {
                startByte = Long.parseLong(parts[0]);
            }

            if (parts.length > 1 && !parts[1].isEmpty()) {
                endByte = Math.min(Long.parseLong(parts[1]), fileSize - 1);
            }

            return new RangeInfo(startByte, endByte);
        }

        return new RangeInfo(0, fileSize - 1);
    }
}
