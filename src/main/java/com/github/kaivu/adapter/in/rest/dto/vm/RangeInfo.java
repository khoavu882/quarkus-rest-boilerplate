package com.github.kaivu.adapter.in.rest.dto.vm;

/**
 * Created by Khoa Vu.
 * Mail: kai.vu.dev@gmail.com
 * Date: 7/27/25
 * Time: 2:20 AM
 */
public record RangeInfo(long startByte, long endByte) {
    public RangeInfo {
        if (startByte < 0 || endByte < startByte) {
            throw new IllegalArgumentException("Invalid range: " + startByte + "-" + endByte);
        }
    }
}
