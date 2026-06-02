package com.bnr.stockmanagement.controller;

import com.bnr.stockmanagement.entity.AuditLog;
import com.bnr.stockmanagement.entity.CallResponse;
import com.bnr.stockmanagement.entity.User;
import com.bnr.stockmanagement.repository.AuditLogRepository;
import com.bnr.stockmanagement.repository.CallResponseRepository;
import com.bnr.stockmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/call-responses")
@CrossOrigin(origins = "http://localhost:5173")
public class CallResponseController {

    @Autowired
    private CallResponseRepository callResponseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/")
    public List<CallResponse> findAll() {
        return callResponseRepository.findAllByHiddenFalseOrderByCreatedAtDesc();
    }

    @GetMapping("/category/{category}")
    public List<CallResponse> findByCategory(@PathVariable String category) {
        String catUpper = category.toUpperCase();
        List<CallResponse> all = callResponseRepository.findAllByHiddenFalseOrderByCreatedAtDesc();
        
        if (catUpper.equals("SYSTEM_AND_OTHER")) {
            return all.stream()
                    .filter(c -> {
                        String cCat = (c.getCategory() != null) ? c.getCategory().toUpperCase() : "";
                        // If it's explicitly system or other, or if it's something unknown (legacy), show it here
                        return cCat.equals("SYSTEM_RELATED") || cCat.equals("OTHER");
                    })
                    .toList();
        }
        
        return all.stream()
                .filter(c -> {
                    String cCat = (c.getCategory() != null) ? c.getCategory().toUpperCase() : "STOCK_RELATED";
                    if (catUpper.equals("STOCK_RELATED")) {
                        // Include both explicit STOCK_RELATED and any legacy/null categories
                        return cCat.equals("STOCK_RELATED") || cCat.isEmpty();
                    }
                    return cCat.equals(catUpper);
                })
                .toList();
    }

    @GetMapping("/user/{userId}")
    public List<CallResponse> findByUserId(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        return callResponseRepository.findAllByUserAndHiddenFalseOrderByCreatedAtDesc(user);
    }

    @PostMapping("/")
    public CallResponse create(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") != null ? Long.parseLong(body.get("userId").toString()) : null;
        String message = body.get("message") != null ? body.get("message").toString() : "";
        String category = body.get("category") != null ? body.get("category").toString() : "STOCK_RELATED";

        if (userId == null) {
            throw new RuntimeException("UserId is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            CallResponse callResponse = CallResponse.builder()
                    .user(user)
                    .message(message)
                    .category(category)
                    .status(CallResponse.Status.PENDING)
                    .hidden(false)
                    .build();

            CallResponse saved = callResponseRepository.save(callResponse);
            
            // Log successful inquiry
            auditLogRepository.save(AuditLog.builder()
                    .user(user)
                    .operation("SEND_CALL")
                    .details("Inquiry sent: [" + category + "] " + (message.length() > 50 ? message.substring(0, 47) + "..." : message))
                    .build());
            
            return saved;
        } catch (Exception e) {
            // Log failed attempt
            auditLogRepository.save(AuditLog.builder()
                    .user(user)
                    .operation("SEND_CALL_FAILED")
                    .details("Error: " + e.getMessage())
                    .build());
            throw e;
        }
    }

    @PatchMapping("/{id}/hide")
    public void hide(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        CallResponse call = callResponseRepository.findById(id).orElse(null);
        if (call == null || userId == null) return;
        String existing = call.getHiddenByUserIds() == null ? "" : call.getHiddenByUserIds();
        if (!existing.contains(String.valueOf(userId))) {
            call.setHiddenByUserIds(existing.isEmpty() ? String.valueOf(userId) : existing + "," + userId);
            callResponseRepository.save(call);
        }
    }

    @PatchMapping("/{id}/hide-soft-delete")
    public CallResponse hideSoftDelete(@PathVariable Long id) {
        CallResponse call = callResponseRepository.findById(id).orElse(null);
        if (call == null) return null;
        call.setHidden(true);
        
        // Log the soft delete action
        auditLogRepository.save(AuditLog.builder()
                .user(call.getUser())
                .operation("CALL_HIDDEN")
                .details("Call response #" + id + " hidden from view")
                .build());
        
        return callResponseRepository.save(call);
    }

    @PatchMapping("/{id}/show-soft-delete")
    public CallResponse showSoftDelete(@PathVariable Long id) {
        CallResponse call = callResponseRepository.findById(id).orElse(null);
        if (call == null) return null;
        call.setHidden(false);
        
        // Log the action
        auditLogRepository.save(AuditLog.builder()
                .user(call.getUser())
                .operation("CALL_SHOWN")
                .details("Call response #" + id + " shown in view")
                .build());
        
        return callResponseRepository.save(call);
    }

    @PostMapping("/{id}/respond")
    public CallResponse respond(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String responseText = body.get("response");
        CallResponse callResponse = callResponseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Call not found"));

        callResponse.setResponse(responseText);
        callResponse.setStatus(CallResponse.Status.RESPONDED);

        return callResponseRepository.save(callResponse);
    }
}
