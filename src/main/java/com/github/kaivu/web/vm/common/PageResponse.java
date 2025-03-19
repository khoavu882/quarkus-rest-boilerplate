package com.github.kaivu.web.vm.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Khoa Vu.
 * Mail: khoavd12@fpt.com
 * Date: 4/29/24
 * Time: 11:18 AM
 */
@Getter
@Setter
@ToString
public class PageResponse<E> implements Serializable {

    private List<E> content;
    private int totalElements;
    private int totalPages;
    private int numberOfElements;
    private int page;
    private boolean hasNext;
    private int size;

    public PageResponse(List<E> content, int totalElements, int page, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.page = page;
        this.size = size;
        this.numberOfElements = content != null ? content.size() : 0;
        this.totalPages = calculateTotalPages(totalElements, size);
        this.hasNext = hasNextPage(page, totalPages);
    }

    private int calculateTotalPages(int totalItems, int pageSize) {
        return (int) Math.round((double) totalItems / pageSize);
    }

    private boolean hasNextPage(int currentPage, int totalPages) {
        return totalPages - currentPage > 0;
    }

    // Method to set content and automatically update related fields
    public void setContent(List<E> content) {
        this.content = content;
        this.numberOfElements = content != null ? content.size() : 0;
    }

    // Method to set total elements and automatically update total pages and hasNext
    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
        this.totalPages = calculateTotalPages(totalElements, this.size);
        this.hasNext = hasNextPage(this.page, this.totalPages);
    }

    // Method to set page and automatically update hasNext
    public void setPage(int page) {
        this.page = page;
        this.hasNext = hasNextPage(page, this.totalPages);
    }

    // Method to set size and automatically update total pages and hasNext
    public void setSize(int size) {
        this.size = size;
        this.totalPages = calculateTotalPages(this.totalElements, size);
        this.hasNext = hasNextPage(this.page, this.totalPages);
    }

    public static class Builder<E> {
        private List<E> content;
        private int totalElements;
        private int page;
        private int size;

        public Builder<E> content(List<E> content) {
            this.content = content;
            return this;
        }

        public Builder<E> totalElements(int totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder<E> page(int page) {
            this.page = page;
            return this;
        }

        public Builder<E> size(int size) {
            this.size = size;
            return this;
        }

        public PageResponse<E> build() {
            return new PageResponse<>(this.content, this.totalElements, this.page, this.size);
        }
    }

    public static <E> Builder<E> builder() {
        return new Builder<>();
    }
}
