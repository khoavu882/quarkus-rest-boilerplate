package com.github.kaivu.adapter.in.rest.dto.request;

import com.github.kaivu.common.exception.ServiceException;
import com.github.kaivu.configuration.handler.ErrorsEnum;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.query.Order;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 4/9/24
 * Time: 4:59 PM
 */
@Getter
@Setter
@ToString
public abstract class PageableRequest implements Serializable {

    @QueryParam("keyword")
    private String keyword;

    @Size
    @DefaultValue("0")
    @QueryParam("page")
    private int page;

    @Size(max = 20)
    @DefaultValue("20")
    @QueryParam("size")
    private int size;

    @DefaultValue("createdDate")
    @QueryParam("sort")
    private String sort;

    public Integer getOffset() {
        return page * size;
    }

    public <T> List<Order<T>> toOrders(String alternative, Class<T> clazz) {
        String sortStr = (sort == null || sort.isBlank()) ? alternative : sort;
        List<String> orders = List.of(sortStr.split("[\\s]*,[\\s]*")); // Split & trim spacing around commas

        return orders.stream()
                .map(order -> {
                    String[] parts = order.strip().split("\\s+");

                    // Validate we have 1 or 2 parts (field and optional direction)
                    if (parts.length == 0 || parts.length > 2) {
                        throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_SORT_PARAMETER);
                    }

                    String fieldName = parts[0];
                    String direction = parts.length > 1 ? parts[1] : "asc";

                    // Validate direction
                    if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
                        throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_SORT_ORDER);
                    }

                    return direction.equalsIgnoreCase("desc")
                            ? Order.desc(clazz, fieldName)
                            : Order.asc(clazz, fieldName);
                })
                .toList();
    }

    /**
     * Build ORDER BY clause string for JPQL queries with alias support
     * This method allows using query aliases like "ed" in JPQL queries
     *
     * @param alias Query alias (e.g., "ed", "user", "product")
     * @param defaultField Default field name if no sort parameter provided
     * @return ORDER BY clause string (without "ORDER BY" prefix)
     */
    public String buildOrderByClause(String alias, String defaultField) {
        String sortStr = (sort == null || sort.isBlank()) ? defaultField : sort;
        List<String> orders = List.of(sortStr.split("[\\s]*,[\\s]*"));

        return orders.stream()
                .map(order -> {
                    String[] parts = order.strip().split("\\s+");

                    if (parts.length == 0 || parts.length > 2) {
                        throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_SORT_PARAMETER);
                    }

                    String fieldName = parts[0];
                    String direction = parts.length > 1 ? parts[1].toUpperCase() : "ASC";

                    if (!direction.equals("ASC") && !direction.equals("DESC")) {
                        throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_SORT_ORDER);
                    }

                    return alias + "." + fieldName + " " + direction;
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse(alias + "." + defaultField + " DESC");
    }
}
