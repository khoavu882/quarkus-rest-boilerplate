package com.github.kaivu.common.constant;

public final class AppHeaderConstant {

    public static final String TRACE_ID = "X-Trace-Id";

    // Legacy trace ID header (keeping for backward compatibility)
    @Deprecated(since = "1.0.0")
    public static final String LEGACY_TRACE_ID = "X-Trace-Id";

    private AppHeaderConstant() {}
}
