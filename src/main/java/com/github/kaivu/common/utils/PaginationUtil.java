package com.github.kaivu.common.utils;

import com.github.kaivu.adapter.in.rest.dto.vm.PageResponse;
import com.github.kaivu.config.ApplicationConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
@ApplicationScoped
public class PaginationUtil {

    private final ApplicationConfiguration config;

    @Inject
    public PaginationUtil(ApplicationConfiguration config) {
        this.config = config;
    }

    public <T> Response.ResponseBuilder generatePaginationResponse(UriBuilder uriBuilder, PageResponse<T> page) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        headers.add(config.pagination.header.totalCount, Long.toString(page.getTotalElements()));
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

    private String prepareLink(UriBuilder uriBuilder, int pageNumber, int pageSize, String relType) {
        return MessageFormat.format(
                config.pagination.header.linkFormat, preparePageUri(uriBuilder, pageNumber, pageSize), relType);
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
