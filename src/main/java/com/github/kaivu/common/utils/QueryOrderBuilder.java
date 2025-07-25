package com.github.kaivu.common.utils;

import com.github.kaivu.adapter.in.rest.dto.request.PageableRequest;
import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.configuration.handler.ErrorsEnum;

import java.util.List;
import java.util.Map;

/**
 * Utility for building flexible ORDER BY clauses for JPQL queries
 * Supports both Hibernate Order objects and JPQL string generation
 */
public class QueryOrderBuilder {

    /**
     * Build ORDER BY clause for JPQL queries with alias support
     * @param pageable PageableRequest containing sort parameters
     * @param alias Query alias (e.g., "ed")
     * @param fieldMappings Map of client field names to entity field names
     * @param defaultSort Default sort field if none provided
     * @return ORDER BY clause string (without "ORDER BY" prefix)
     */
    public static String buildOrderByClause(PageableRequest pageable, String alias,
                                           Map<String, String> fieldMappings, String defaultSort) {
        String sortStr = (pageable.getSort() == null || pageable.getSort().isBlank())
                        ? defaultSort : pageable.getSort();

        List<String> orders = List.of(sortStr.split("[\\s]*,[\\s]*"));

        return orders.stream()
                .map(order -> {
                    String[] parts = order.strip().split("\\s+");
                    String fieldName = parts[0];
                    String direction = parts.length > 1 ? parts[1].toUpperCase() : "ASC";

                    if (!direction.equals("ASC") && !direction.equals("DESC")) {
                        throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_SORT_ORDER);
                    }

                    // Map client field name to entity field name
                    String entityField = fieldMappings.getOrDefault(fieldName, fieldName);

                    return alias + "." + entityField + " " + direction;
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse(alias + "." + defaultSort + " DESC");
    }

    /**
     * Build simple ORDER BY clause with default mappings
     */
    public static String buildOrderByClause(PageableRequest pageable, String alias, String defaultSort) {
        return buildOrderByClause(pageable, alias, Map.of(), defaultSort);
    }
}
