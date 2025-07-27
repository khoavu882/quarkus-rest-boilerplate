package com.github.kaivu.adapter.in.rest.dto.vm;

import java.io.InputStream;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 7/27/25
 * Time: 2:20 AM
 */
public record StreamingResponse(
        RangeInfo rangeInfo, long fileSize, String contentType, String etag, InputStream inputStream) {}
