package com.github.kaivu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 7/27/25
 * Time: 2:23 AM
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "media_files")
public class MediaFile extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String objectName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String etag;
}
