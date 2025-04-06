package com.example.domain.common;

/**
 * 페이지네이션을 위한 도메인 객체
 */
public class Pageable {
    private final int page;
    private final int size;
    private final String sortBy;
    private final SortDirection direction;
    
    public Pageable(int page, int size) {
        this(page, size, "id", SortDirection.DESC);
    }
    
    public Pageable(int page, int size, String sortBy, SortDirection direction) {
        validatePage(page);
        validateSize(size);
        this.page = page;
        this.size = size;
        this.sortBy = sortBy != null ? sortBy : "id";
        this.direction = direction != null ? direction : SortDirection.DESC;
    }
    
    private void validatePage(int page) {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
    }
    
    private void validateSize(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기는 1 이상이어야 합니다.");
        }
        if (size > 100) {
            throw new IllegalArgumentException("페이지 크기는 최대 100입니다.");
        }
    }
    
    public int getPage() {
        return page;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public SortDirection getDirection() {
        return direction;
    }
    
    public String getDirectionAsString() {
        return direction.name();
    }
    
    @Override
    public String toString() {
        return "Pageable{" +
                "page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", direction=" + direction +
                '}';
    }
}
