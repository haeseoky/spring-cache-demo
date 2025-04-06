package com.example.domain.common;

import java.util.Collections;
import java.util.List;

/**
 * 페이지네이션 결과를 담는 도메인 객체
 */
public class Page<T> {
    private final List<T> content;
    private final int number;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    
    public Page(List<T> content, int number, int size, long totalElements) {
        this.content = content != null ? content : Collections.emptyList();
        this.number = number;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }
    
    public List<T> getContent() {
        return content;
    }
    
    public int getNumber() {
        return number;
    }
    
    public int getSize() {
        return size;
    }
    
    public long getTotalElements() {
        return totalElements;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public boolean isFirst() {
        return number == 0;
    }
    
    public boolean isLast() {
        return number >= totalPages - 1;
    }
    
    public boolean hasContent() {
        return !content.isEmpty();
    }
    
    @Override
    public String toString() {
        return "Page{" +
                "content.size=" + content.size() +
                ", number=" + number +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                '}';
    }
}
