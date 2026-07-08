package com.github.kaivu.domain.supplier;

import com.github.kaivu.domain.enumeration.ActionStatus;

import java.util.function.Supplier;

/**
 * Created by Khoa Vu.
 * Mail: kai.vu.dev@gmail.com
 * Date: 3/13/25
 * Time: 11:20 AM
 */
public class DeletedStatusSupplier implements Supplier<String> {
    @Override
    public String get() {
        return ActionStatus.DELETED.name();
    }
}
