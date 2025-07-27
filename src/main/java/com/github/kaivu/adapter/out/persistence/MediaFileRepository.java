package com.github.kaivu.adapter.out.persistence;

import com.github.kaivu.application.port.IMediaFileRepository;
import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.domain.MediaFile;
import com.github.kaivu.domain.MediaFile_;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

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
public class MediaFileRepository implements IMediaFileRepository {

    private static final String CACHE_PREFIX = MediaFile_.class_.getName();

    private final CacheService cacheService;

    @Inject
    public MediaFileRepository(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public Uni<Optional<MediaFile>> findByBucketAndObject(String bucketName, String objectName) {
        String cacheKey = cacheService.generateKey(CACHE_PREFIX, bucketName, objectName);

        // Use cache-first strategy with getOrCompute
        return cacheService
                .getOrCompute(
                        cacheKey, MediaFile.class, () -> findFromDatabase(bucketName, objectName), Duration.ofHours(1))
                .map(Optional::ofNullable);
    }

    private Uni<MediaFile> findFromDatabase(String bucketName, String objectName) {
        return Panache.getSession().chain(session -> session.createQuery(
                        "SELECT m FROM MediaFile m WHERE m.bucketName = :bucketName AND m.objectName = :objectName",
                        MediaFile.class)
                .setParameter("bucketName", bucketName)
                .setParameter("objectName", objectName)
                .getSingleResultOrNull());
    }

    @Override
    public Uni<MediaFile> save(MediaFile mediaFile) {
        return Panache.withTransaction(() -> {
                    if (mediaFile.getId() == null) {
                        // New entity - persist
                        return Panache.getSession().chain(session -> session.persist(mediaFile)
                                .chain(ignored -> session.flush())
                                .map(ignored -> mediaFile));
                    } else {
                        // Existing entity - merge
                        return Panache.getSession().chain(session -> session.merge(mediaFile)
                                .chain(merged -> session.flush().map(ignored -> merged)));
                    }
                })
                .chain(savedMedia -> {
                    // Update cache after successful save
                    String cacheKey = cacheService.generateKey(
                            CACHE_PREFIX, savedMedia.getBucketName(), savedMedia.getObjectName());
                    return cacheService
                            .set(cacheKey, savedMedia, Duration.ofHours(1))
                            .map(ignored -> savedMedia);
                });
    }

    @Override
    public Uni<Void> deleteByBucketAndObject(String bucketName, String objectName) {
        return Panache.withTransaction(() -> Panache.getSession()
                        .chain(session -> session.createMutationQuery(
                                        "DELETE FROM MediaFile m WHERE m.bucketName = :bucketName AND m.objectName = :objectName")
                                .setParameter("bucketName", bucketName)
                                .setParameter("objectName", objectName)
                                .executeUpdate())
                        .chain(ignored -> Panache.getSession().chain(Mutiny.Session::flush))
                        .replaceWithVoid())
                .chain(ignored -> {
                    // Remove from cache after successful deletion
                    String cacheKey = cacheService.generateKey(CACHE_PREFIX, bucketName, objectName);
                    return cacheService.delete(cacheKey).replaceWithVoid();
                });
    }
}
