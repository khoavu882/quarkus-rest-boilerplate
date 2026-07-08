package com.github.kaivu.application.port;

import com.github.kaivu.domain.MediaFile;
import io.smallrye.mutiny.Uni;

import java.util.Optional;

/**
 * Created by Khoa Vu.
 * Mail: kai.vu.dev@gmail.com
 * Date: 7/27/25
 * Time: 2:25 AM
 */
public interface IMediaFileRepository {
    Uni<Optional<MediaFile>> findByBucketAndObject(String bucketName, String objectName);

    Uni<MediaFile> save(MediaFile mediaFile);

    Uni<Void> deleteByBucketAndObject(String bucketName, String objectName);
}
