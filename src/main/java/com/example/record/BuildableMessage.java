package com.example.record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 빌더 패턴을 구현한 Record
 * - 복잡한 객체의 단계적 생성
 * - 가독성과 사용성 개선
 */
public record BuildableMessage(String sender, String recipient, String subject, String body, 
                               boolean urgent, List<String> attachments) {
    
    // 방어적 복사를 사용한 생성자
    public BuildableMessage {
        if (sender == null || recipient == null) {
            throw new IllegalArgumentException("발신자와 수신자는 필수입니다");
        }
        attachments = Collections.unmodifiableList(new ArrayList<>(attachments));
    }
    
    // 빌더 클래스
    public static class Builder {
        private String sender;
        private String recipient;
        private String subject = "";
        private String body = "";
        private boolean urgent = false;
        private List<String> attachments = new ArrayList<>();
        
        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }
        
        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }
        
        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }
        
        public Builder body(String body) {
            this.body = body;
            return this;
        }
        
        public Builder urgent(boolean urgent) {
            this.urgent = urgent;
            return this;
        }
        
        public Builder addAttachment(String attachment) {
            this.attachments.add(attachment);
            return this;
        }
        
        public BuildableMessage build() {
            if (sender == null || recipient == null) {
                throw new IllegalStateException("발신자와 수신자는 필수입니다");
            }
            return new BuildableMessage(sender, recipient, subject, body, urgent, 
                                       List.copyOf(attachments)); // 불변 리스트로 변환
        }
    }
    
    // 정적 빌더 메서드
    public static Builder builder() {
        return new Builder();
    }
    
    // 메시지 포맷 메서드
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("From: ").append(sender).append("\n");
        sb.append("To: ").append(recipient).append("\n");
        
        if (!subject.isEmpty()) {
            sb.append("Subject: ").append(subject);
            if (urgent) sb.append(" [URGENT]");
            sb.append("\n");
        }
        
        sb.append("\n").append(body).append("\n");
        
        if (!attachments.isEmpty()) {
            sb.append("\nAttachments: ").append(String.join(", ", attachments));
        }
        
        return sb.toString();
    }
}
