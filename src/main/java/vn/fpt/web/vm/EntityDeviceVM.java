package vn.fpt.web.vm;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/13/24
 * Time: 11:41 AM
 */
@Getter
@Setter
@ToString
public class EntityDeviceVM implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID id;

    private String name;

    private String description;

    private JsonObject metadata = new JsonObject();
}
