package com.github.kaivu.adapter.out.persistence;

import com.github.kaivu.application.port.IMediaFileRepository;
import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.domain.MediaFile;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.util.Optional;

/**
 * MediaFile repository with Redis caching support
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 7/27/25
 * Time: 2:25 AM
 */
@ApplicationScoped
public class MediaFileRepository implements PanacheRepositoryBase<MediaFile, Long>, IMediaFileRepository {

    private final CacheService cacheService;

    @Inject
    public MediaFileRepository(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    private String getCachePrefix() {
        return "MediaFile";
    }

    @Override
    public Uni<Optional<MediaFile>> findByBucketAndObject(String bucketName, String objectName) {
        String cacheKey = cacheService.generateKey(getCachePrefix(), bucketName, objectName);

        // Use cache-first strategy with getOrCompute
        return cacheService
                .getOrCompute(
                        cacheKey, MediaFile.class, () -> findFromDatabase(bucketName, objectName), Duration.ofHours(1))
                .map(Optional::ofNullable);
    }

    private Uni<MediaFile> findFromDatabase(String bucketName, String objectName) {
        return find("bucketName = ?1 and objectName = ?2", bucketName, objectName)
                .firstResult();
    }

    @Override
    public Uni<MediaFile> save(MediaFile mediaFile) {
        Uni<MediaFile> saveOperation;

        if (mediaFile.getId() == null) {
            saveOperation = persistAndFlush(mediaFile);
        } else {
            saveOperation = persistAndFlush(mediaFile);
        }

        return saveOperation.chain(savedMedia -> {
            String cacheKey =
                    cacheService.generateKey(getCachePrefix(), savedMedia.getBucketName(), savedMedia.getObjectName());
            return cacheService.set(cacheKey, savedMedia, Duration.ofHours(1)).map(ignored -> savedMedia);
        });
    }

    @Override
    public Uni<Void> deleteByBucketAndObject(String bucketName, String objectName) {
        return delete("bucketName = ?1 and objectName = ?2", bucketName, objectName)
                .chain(deletedCount -> {
                    // Remove from cache after successful deletion
                    String cacheKey = cacheService.generateKey(getCachePrefix(), bucketName, objectName);
                    return cacheService.delete(cacheKey).replaceWithVoid();
                });
    }
}
