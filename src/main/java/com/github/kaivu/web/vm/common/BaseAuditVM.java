package com.github.kaivu.web.vm.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 3/13/25
 * Time: 11:42 AM
 */
@Getter
@Setter
@ToString
public class BaseAuditVM extends UpdateAuditVM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String createdBy;

    private Instant createdDate;
}
