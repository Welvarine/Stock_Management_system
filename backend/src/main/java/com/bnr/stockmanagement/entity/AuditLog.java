package com.bnr.stockmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String operation; // e.g., "LOGIN", "CREATE_REQUEST", "APPROVE_REQUEST"

    @Column(columnDefinition = "TEXT")
    private String details;   // e.g., "Request ID: 15", "User logged in"

    @CreationTimestamp
    private LocalDateTime timestamp;

    // Explicit Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Manual Builder
    public static AuditLogBuilder builder() { return new AuditLogBuilder(); }
    public static class AuditLogBuilder {
        private AuditLog log = new AuditLog();
        public AuditLogBuilder user(User user) { log.user = user; return this; }
        public AuditLogBuilder operation(String operation) { log.operation = operation; return this; }
        public AuditLogBuilder details(String details) { log.details = details; return this; }
        public AuditLog build() { return log; }
    }
}
