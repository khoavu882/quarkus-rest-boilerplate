package vn.fpt.constant;

public final class ErrorsKeyConstant {

    // Default error key
    public static final String ERROR_NON_DEFINED = "error_non_defined";
    public static final String BUNDLE_DOES_NOT_EXIST = "bundle_does_not_exist";

    // http request defined error key constants
    public static final String INTERNAL_SERVER_ERROR = "internal_server_error";
    public static final String NOT_ACCEPTABLE = "not_acceptable";
    public static final String PERMISSION_DENIED = "permission_denied";
    public static final String UNAUTHORIZED = "unauthorized";
    public static final String INVALID_REDIRECT = "invalid_redirect";

    // User defined error key constants
    public static final String DATABASE_CONNECTION_FAILED = "database_connection_failed";
    public static final String CLIENT_BAD_REQUEST = "client_bad_request";
    public static final String CONFLICT = "conflict";
    public static final String FORMAT_INVALID = "format_invalid";
    public static final String ID_CONFLICT = "id_conflict";
    public static final String ID_INVALID = "id_invalid";
    public static final String INVALID_SORT_ORDER = "invalid_sort_order";
    public static final String INVALID_SORT_PARAMETER = "invalid_sort_parameter";
    public static final String INVALID_TIME_RANGE = "invalid_time_range";
    public static final String NOT_FOUND = "not_found";

    private ErrorsKeyConstant() {}
}
