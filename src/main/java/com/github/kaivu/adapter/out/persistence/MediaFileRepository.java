package com.github.kaivu.adapter.out.persistence;

import com.github.kaivu.application.port.IMediaFileRepository;
import com.github.kaivu.application.service.CacheService;
import com.github.kaivu.config.AppConfiguration;
import com.github.kaivu.domain.MediaFile;
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

    private final Mutiny.SessionFactory sessionFactory;
    private final CacheService cacheService;
    private final AppConfiguration config;

    @Inject
    public MediaFileRepository(
            Mutiny.SessionFactory sessionFactory, CacheService cacheService, AppConfiguration config) {
        this.sessionFactory = sessionFactory;
        this.cacheService = cacheService;
        this.config = config;
    }

    private String getCachePrefix() {
        return config.cache().prefix().mediaFile();
    }

    @Override
    public Uni<Optional<MediaFile>> findByBucketAndObject(String bucketName, String objectName) {
        String cacheKey = cacheService.generateKey(getCachePrefix(), bucketName, objectName);

        // Use cache-first strategy with getOrCompute
        return cacheService
                .getOrCompute(
                        cacheKey,
                        MediaFile.class,
                        () -> findFromDatabase(bucketName, objectName),
                        Duration.ofMillis(config.cache().mediaFile().ttlMs()))
                .map(Optional::ofNullable);
    }

    private Uni<MediaFile> findFromDatabase(String bucketName, String objectName) {
        return sessionFactory.withSession(session -> session.createQuery(
                        "FROM MediaFile mf WHERE mf.bucketName = :bucketName AND mf.objectName = :objectName",
                        MediaFile.class)
                .setParameter("bucketName", bucketName)
                .setParameter("objectName", objectName)
                .getSingleResultOrNull());
    }

    @Override
    public Uni<MediaFile> save(MediaFile mediaFile) {
        Uni<MediaFile> saveOperation;

        if (mediaFile.getId() == null) {
            // Create new entity
            saveOperation = sessionFactory.withTransaction(
                    (session, tx) -> session.persist(mediaFile).replaceWith(mediaFile));
        } else {
            // Update existing entity
            saveOperation = sessionFactory.withTransaction(
                    (session, tx) -> session.merge(mediaFile).replaceWith(mediaFile));
        }

        return saveOperation.chain(savedMedia -> {
            String cacheKey =
                    cacheService.generateKey(getCachePrefix(), savedMedia.getBucketName(), savedMedia.getObjectName());
            return cacheService
                    .set(
                            cacheKey,
                            savedMedia,
                            Duration.ofMillis(config.cache().mediaFile().ttlMs()))
                    .replaceWith(savedMedia);
        });
    }

    @Override
    public Uni<Void> deleteByBucketAndObject(String bucketName, String objectName) {
        return sessionFactory
                .withTransaction((session, tx) -> session.createMutationQuery(
                                "DELETE FROM MediaFile mf WHERE mf.bucketName = :bucketName AND mf.objectName = :objectName")
                        .setParameter("bucketName", bucketName)
                        .setParameter("objectName", objectName)
                        .executeUpdate())
                .chain(deletedCount -> {
                    // Remove from cache after successful deletion
                    String cacheKey = cacheService.generateKey(getCachePrefix(), bucketName, objectName);
                    return cacheService.delete(cacheKey).replaceWithVoid();
                });
    }
}
