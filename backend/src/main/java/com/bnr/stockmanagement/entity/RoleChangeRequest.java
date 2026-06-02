package com.bnr.stockmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_change_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private User.Role requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    private String rejectionReason;

    private String otp;

    @Column(length = 2000)
    @Builder.Default
    private String hiddenByUserIds = "";

    @Column
    @Builder.Default
    private Boolean hidden = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED
    }

    // Explicit Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public User.Role getRequestedRole() { return requestedRole; }
    public void setRequestedRole(User.Role requestedRole) { this.requestedRole = requestedRole; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
    public Boolean getHidden() { return hidden; }
    public void setHidden(Boolean hidden) { this.hidden = hidden; }
    public String getHiddenByUserIds() { return hiddenByUserIds; }
    public void setHiddenByUserIds(String hiddenByUserIds) { this.hiddenByUserIds = hiddenByUserIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Manual Builder
    public static RoleChangeRequestBuilder builder() { return new RoleChangeRequestBuilder(); }
    public static class RoleChangeRequestBuilder {
        private RoleChangeRequest req = new RoleChangeRequest();
        public RoleChangeRequestBuilder user(User user) { req.user = user; return this; }
        public RoleChangeRequestBuilder requestedRole(User.Role requestedRole) { req.requestedRole = requestedRole; return this; }
        public RoleChangeRequestBuilder status(RequestStatus status) { req.status = status; return this; }
        public RoleChangeRequestBuilder otp(String otp) { req.otp = otp; return this; }
        public RoleChangeRequest build() { return req; }
    }
}
