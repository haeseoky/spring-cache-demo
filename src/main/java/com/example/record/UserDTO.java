package com.example.record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Data JPA와 함께 사용하는 Record DTO
 * - 데이터 전송 객체로 활용
 * - 엔티티 변환 로직 포함
 */
public record UserDTO(Long id, String username, String email, LocalDateTime createdAt, List<RoleDTO> roles) {
    // 내부 Role DTO
    public record RoleDTO(Long id, String name) {
        // 엔티티에서 DTO로 변환하는 메서드 (실제 엔티티는 주석 처리)
        /*
        public static RoleDTO fromEntity(Role role) {
            return new RoleDTO(role.getId(), role.getName());
        }
        */
    }
    
    // 기본 생성자 (역할 없는 버전)
    public UserDTO(Long id, String username, String email, LocalDateTime createdAt) {
        this(id, username, email, createdAt, List.of());
    }

    // Entity에서 DTO로 변환하는 정적 팩토리 메서드 (실제 엔티티는 주석 처리)
    /*
    public static UserDTO fromEntity(User user) {
        List<RoleDTO> roleDTOs = user.getRoles().stream()
            .map(RoleDTO::fromEntity)
            .collect(Collectors.toList());
            
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getCreatedAt(),
            roleDTOs
        );
    }
    
    public static List<UserDTO> fromEntities(List<User> users) {
        return users.stream()
            .map(UserDTO::fromEntity)
            .collect(Collectors.toList());
    }
    */
    
    // 사용자 정보 요약
    public String getSummary() {
        return String.format("%s (%s) - %s", username, email, 
            roles.isEmpty() ? "일반 사용자" : "역할: " + roles.stream()
                .map(RoleDTO::name)
                .collect(Collectors.joining(", ")));
    }
}
