package com.github.kaivu.adapter.in.rest.dto.request;

import com.github.kaivu.domain.enumeration.ActionStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Khoa Vu.
 * Mail: khoavu882@gmail.com
 * Date: 12/13/24
 * Time: 11:41 AM
 */
@Getter
@Setter
@ToString
public class EntityDeviceFilters extends PageableRequest implements Serializable {

    private String name;
    private ActionStatus status;
}
