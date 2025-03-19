package com.github.kaivu.web.vm;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/13/24
 * Time: 11:41 AM
 */
@Getter
@Setter
@ToString
public class EntityDeviceDetailsVM extends EntityDeviceVM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private JsonObject metadata = new JsonObject();

    private Instant createdDate;
}
