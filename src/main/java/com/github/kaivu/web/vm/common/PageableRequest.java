package com.github.kaivu.web.vm.common;

import com.github.kaivu.web.errors.ErrorsEnum;
import com.github.kaivu.web.errors.exceptions.ServiceException;
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

    @DefaultValue("createdAt")
    @QueryParam("sort")
    private String sort;

    public Integer getOffset() {
        return page * size;
    }

    public <T> List<Order<T>> toOrders(String alternative, Class<T> clazz) {
        String sortStr = (sort == null || sort.isBlank()) ? alternative : sort;
        List<String> orders = List.of(sortStr.split("[\s]*,[\s]*")); // Split & trim spacing around commas

        return orders.stream()
                .map(order -> {
                    String[] parts = order.strip().split("\\s+");

                    return switch (parts.length) {
                        case 1 -> Order.asc(clazz, parts[0]);
                        case 2 -> {
                            if (!parts[1].equalsIgnoreCase("asc") && !parts[1].equalsIgnoreCase("desc")) {
                                throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_SORT_ORDER);
                            }

                            yield parts[1].equalsIgnoreCase("desc")
                                    ? Order.desc(clazz, parts[0])
                                    : Order.asc(clazz, parts[0]);
                        }
                        default -> throw new ServiceException(ErrorsEnum.SYSTEM_INVALID_SORT_PARAMETER);
                    };
                })
                .toList();
    }
}
