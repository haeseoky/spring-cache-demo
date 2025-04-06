package com.example.common.dto;

/**
 * 페이지네이션 요청 객체
 */
public class PageRequest {
    private int page;
    private int size;
    private String sortBy;
    private String direction;

    public PageRequest() {
        this.page = 0;
        this.size = 10;
        this.sortBy = "id";
        this.direction = "DESC";
    }

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
        this.sortBy = "id";
        this.direction = "DESC";
    }

    public PageRequest(int page, int size, String sortBy, String direction) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "PageRequest{" +
                "page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", direction='" + direction + '\'' +
                '}';
    }
}
