package com.bnr.stockmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "call_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = true)
    @Builder.Default
    private String category = "STOCK_RELATED";

    @Column(length = 1000)
    private String response;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(length = 2000)
    @Builder.Default
    private String hiddenByUserIds = "";

    @Column
    @Builder.Default
    private Boolean hidden = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, RESPONDED
    }

    // Explicit Getters and Setters for compatibility if needed
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Boolean getHidden() { return hidden; }
    public void setHidden(Boolean hidden) { this.hidden = hidden; }
    public String getHiddenByUserIds() { return hiddenByUserIds; }
    public void setHiddenByUserIds(String hiddenByUserIds) { this.hiddenByUserIds = hiddenByUserIds; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Manual Builder
    public static CallResponseBuilder builder() { return new CallResponseBuilder(); }
    public static class CallResponseBuilder {
        private CallResponse cr = new CallResponse();
        public CallResponseBuilder user(User user) { cr.user = user; return this; }
        public CallResponseBuilder message(String message) { cr.message = message; return this; }
        public CallResponseBuilder category(String category) { cr.category = category; return this; }
        public CallResponseBuilder status(Status status) { cr.status = status; return this; }
        public CallResponse build() { return cr; }
    }
}
