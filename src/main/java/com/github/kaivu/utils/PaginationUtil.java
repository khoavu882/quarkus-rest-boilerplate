package com.fpt.cads.utils;

import com.fpt.cads.web.vm.PageResponse;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.text.MessageFormat;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 4/9/24
 * Time: 11:22 PM
 */
public final class PaginationUtil {
    private static final String HEADER_X_TOTAL_COUNT = "X-Total-Count";
    private static final String HEADER_LINK_FORMAT = "<{0}>; rel=\"{1}\"";

    private PaginationUtil() {}

    public static <T> Response.ResponseBuilder generatePaginationResponse(UriBuilder uriBuilder, PageResponse<T> page) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        headers.add(HEADER_X_TOTAL_COUNT, Long.toString(page.getTotalElements()));
        int pageNumber = page.getPage();
        int pageSize = page.getSize();
        StringBuilder link = new StringBuilder();

        if (pageNumber < page.getTotalPages() - 1) {
            link.append(prepareLink(uriBuilder, pageNumber + 1, pageSize, "next"))
                    .append(",");
        }

        if (pageNumber > 0) {
            link.append(prepareLink(uriBuilder, pageNumber - 1, pageSize, "prev"))
                    .append(",");
        }

        link.append(prepareLink(uriBuilder, page.getTotalPages() - 1, pageSize, "last"))
                .append(",")
                .append(prepareLink(uriBuilder, 0, pageSize, "first"));
        headers.add("Link", link.toString());

        Response.ResponseBuilder responseBuilder = Response.ok().entity(page.getContent());

        headers.forEach(responseBuilder::header);
        return responseBuilder;
    }

    private static String prepareLink(UriBuilder uriBuilder, int pageNumber, int pageSize, String relType) {
        return MessageFormat.format(HEADER_LINK_FORMAT, preparePageUri(uriBuilder, pageNumber, pageSize), relType);
    }

    private static String preparePageUri(UriBuilder uriBuilder, int pageNumber, int pageSize) {
        return uriBuilder
                .resolveTemplate("page", new Object[] {Integer.toString(pageNumber)})
                .replaceQueryParam("size", Integer.toString(pageSize))
                .toTemplate()
                .replace(",", "%2C")
                .replace(";", "%3B");
    }
}
