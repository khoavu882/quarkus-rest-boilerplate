package com.github.kaivu.adapter.in.rest.dto.vm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Created by Khoa Vu.
 * Mail: kai.vu.dev@gmail.com
 * Date: 3/13/25
 * Time: 11:42 AM
 */
@Getter
@Setter
@ToString
public class FullAuditVM extends BaseAuditVM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}
