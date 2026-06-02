package com.bnr.stockmanagement.controller;

import com.bnr.stockmanagement.entity.AuditLog;
import com.bnr.stockmanagement.entity.RoleChangeRequest;
import com.bnr.stockmanagement.entity.User;
import com.bnr.stockmanagement.repository.AuditLogRepository;
import com.bnr.stockmanagement.repository.RoleChangeRequestRepository;
import com.bnr.stockmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.bnr.stockmanagement.service.EmailService;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleChangeRequestRepository roleChangeRequestRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @GetMapping("/")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User findOne(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping("/")
    public User create(@RequestBody User user) {
        // Check if username or email already exists to provide a clear error
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findAll().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(user.getEmail()))) {
            throw new RuntimeException("Email already exists");
        }

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // Default role and status
            if (user.getRole() == null) {
                user.setRole(User.Role.requester);
            }
            user.setStatus("OFFLINE");

            User savedUser = userRepository.save(user);

            System.out.println("New User Signed Up: " + savedUser.getUsername());

            auditLogRepository.save(AuditLog.builder()
                    .user(savedUser)
                    .operation("SIGNUP")
                    .details("User signed up with role " + savedUser.getRole())
                    .build());

            return savedUser;
        } catch (Exception e) {
            System.out.println("CRITICAL SIGNUP FAILURE:");
            e.printStackTrace();
            throw new RuntimeException("Signup failed: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("requesters",
                userRepository.findAll().stream().filter(u -> u.getRole() == User.Role.requester).count());
        stats.put("approvers",
                userRepository.findAll().stream().filter(u -> u.getRole() == User.Role.approver).count());
        stats.put("admins", userRepository.findAll().stream().filter(u -> u.getRole() == User.Role.admin).count());
        return stats;
    }

    @PostMapping("/role-request")
    public RoleChangeRequest createRoleRequest(@RequestBody Map<String, String> request) {
        Long userId = Long.parseLong(request.get("userId"));
        User.Role requestedRole = User.Role.valueOf(request.get("requestedRole"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if there is already a pending request
        roleChangeRequestRepository.findByUserAndStatus(user, RoleChangeRequest.RequestStatus.PENDING)
                .ifPresent(r -> {
                    throw new RuntimeException("Already have a pending request");
                });

        RoleChangeRequest roleRequest = RoleChangeRequest.builder()
                .user(user)
                .requestedRole(requestedRole)
                .status(RoleChangeRequest.RequestStatus.PENDING)
                .hidden(false)
                .build();

        return roleChangeRequestRepository.save(roleRequest);
    }

    @GetMapping("/role-requests")
    public List<RoleChangeRequest> getRoleRequests() {
        return roleChangeRequestRepository.findAllByHiddenFalse();
    }

    @PatchMapping("/role-requests/{id}/hide")
    public void hideRoleRequest(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        RoleChangeRequest req = roleChangeRequestRepository.findById(id).orElse(null);
        if (req == null || userId == null) return;
        String existing = req.getHiddenByUserIds() == null ? "" : req.getHiddenByUserIds();
        if (!existing.contains(String.valueOf(userId))) {
            req.setHiddenByUserIds(existing.isEmpty() ? String.valueOf(userId) : existing + "," + userId);
            roleChangeRequestRepository.save(req);
        }
    }

    @PatchMapping("/role-requests/{id}/hide-soft-delete")
    public RoleChangeRequest hideRoleRequestSoftDelete(@PathVariable Long id) {
        RoleChangeRequest req = roleChangeRequestRepository.findById(id).orElse(null);
        if (req == null) return null;
        req.setHidden(true);
        
        // Log the soft delete action
        auditLogRepository.save(AuditLog.builder()
                .user(req.getUser())
                .operation("ROLE_REQUEST_HIDDEN")
                .details("Role request #" + id + " hidden from view")
                .build());
        
        return roleChangeRequestRepository.save(req);
    }

    @PatchMapping("/role-requests/{id}/show-soft-delete")
    public RoleChangeRequest showRoleRequestSoftDelete(@PathVariable Long id) {
        RoleChangeRequest req = roleChangeRequestRepository.findById(id).orElse(null);
        if (req == null) return null;
        req.setHidden(false);
        
        // Log the action
        auditLogRepository.save(AuditLog.builder()
                .user(req.getUser())
                .operation("ROLE_REQUEST_SHOWN")
                .details("Role request #" + id + " shown in view")
                .build());
        
        return roleChangeRequestRepository.save(req);
    }

    @PostMapping("/role-requests/{id}/approve")
    public RoleChangeRequest approveRoleRequest(@PathVariable Long id) {
        RoleChangeRequest roleRequest = roleChangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        roleRequest.setStatus(RoleChangeRequest.RequestStatus.APPROVED);

        // Generate a 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        roleRequest.setOtp(otp);

        System.out.println("OTP for user " + roleRequest.getUser().getUsername() + " ("
                + roleRequest.getUser().getEmail() + "): " + otp);

        auditLogRepository.save(AuditLog.builder()
                .user(roleRequest.getUser())
                .operation("ROLE_REQUEST_APPROVED")
                .details("Role change to " + roleRequest.getRequestedRole() + " approved. OTP generated.")
                .build());

        // Send real-time OTP email
        try {
            emailService.sendOTP(roleRequest.getUser().getEmail(), otp);
            System.out.println("OTP Email sent successfully to " + roleRequest.getUser().getEmail());
        } catch (Exception e) {
            System.out.println("CRITICAL: Failed to send OTP email to " + roleRequest.getUser().getEmail() + ". Error: " + e.getMessage());
        }

        return roleChangeRequestRepository.save(roleRequest);
    }

    @PostMapping("/role-requests/{id}/reject")
    public RoleChangeRequest rejectRoleRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        RoleChangeRequest roleRequest = roleChangeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        roleRequest.setStatus(RoleChangeRequest.RequestStatus.REJECTED);
        roleRequest.setRejectionReason(body.get("reason"));

        return roleChangeRequestRepository.save(roleRequest);
    }

    @PostMapping("/verify-role-otp")
    public User verifyRoleOtp(@RequestBody Map<String, String> body) {
        Long userId = Long.parseLong(body.get("userId"));
        String otp = body.get("otp");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<RoleChangeRequest> userRequests = roleChangeRequestRepository.findByUser(user);

        RoleChangeRequest roleRequest = userRequests.stream()
                .filter(r -> r.getStatus() == RoleChangeRequest.RequestStatus.APPROVED && otp != null && otp.trim().equals(r.getOtp()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        User.Role oldRole = user.getRole();
        // Apply new role
        user.setRole(roleRequest.getRequestedRole());
        userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .operation("ROLE_VERIFIED")
                .details("Role changed from " + oldRole + " to " + user.getRole())
                .build());

        // Delete all role change requests for this user upon successful role verification
        roleChangeRequestRepository.deleteAll(userRequests);

        return user;
    }
    @PostMapping("/login")
    public User login(@RequestBody User loginUser) {
        String username = loginUser.getUsername() != null ? loginUser.getUsername().trim() : "";
        String password = loginUser.getPassword() != null ? loginUser.getPassword().trim() : "";
        
        System.out.println("Login attempt for: " + username);
        
        if (password.isEmpty()) {
            System.out.println("Login failed: Password was not provided in the request JSON for " + username);
            throw new RuntimeException("Invalid username or password");
        }

        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);

        if (user == null) {
            System.out.println("Login failed: User '" + username + "' not found.");
            throw new RuntimeException("Invalid username or password");
        }

        String storedPassword = user.getPassword();
        if (storedPassword == null || storedPassword.isEmpty()) {
            System.out.println("Login failed: Stored password for '" + username + "' is EMPTY or NULL.");
            throw new RuntimeException("Invalid username or password");
        }

        boolean matches = false;
        try {
            if (storedPassword != null && storedPassword.startsWith("$2a$")) {
                matches = passwordEncoder.matches(password, storedPassword);
            }
        } catch (Exception e) {
            System.out.println("Security check failed for " + username);
        }

        if (matches) {
            user.setStatus("ONLINE");
            userRepository.save(user);
            auditLogRepository.save(AuditLog.builder()
                    .user(user)
                    .operation("LOGIN")
                    .details("User logged in successfully")
                    .build());
            return user;
        }

        throw new RuntimeException("Invalid username or password");
    }

    @GetMapping("/logs")
    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }

    @PostMapping("/log")
    public AuditLog createLog(@RequestBody Map<String, String> body) {
        Long userId = body.get("userId") != null ? Long.parseLong(body.get("userId")) : null;
        String operation = body.get("operation");
        String details = body.get("details");

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        return auditLogRepository.save(AuditLog.builder()
                .user(user)
                .operation(operation)
                .details(details)
                .build());
    }

    @PostMapping("/logout/{id}")
    public void logout(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus("OFFLINE");
            userRepository.save(user);

            auditLogRepository.save(AuditLog.builder()
                    .user(user)
                    .operation("LOGOUT")
                    .details("User logged out successfully")
                    .build());
        }
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        if (identifier == null || identifier.isEmpty()) {
            throw new RuntimeException("Username or Email is required");
        }

        User user = userRepository.findByEmailIgnoreCase(identifier.trim())
                .orElseGet(() -> userRepository.findByUsernameIgnoreCase(identifier.trim()).orElse(null));

        if (user == null) {
            throw new RuntimeException("No account found with that identifier");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setResetOtp(otp);
        userRepository.save(user);

        try {
            emailService.sendOTP(user.getEmail(), otp);
            System.out.println("Password reset OTP sent to " + user.getEmail());
        } catch (Exception e) {
            System.out.println("Failed to send OTP: " + e.getMessage());
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "OTP sent successfully to your registered email");
        return response;
    }

    @PostMapping("/reset-password")
    public User resetPassword(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");

        if (identifier == null || otp == null || newPassword == null) {
            throw new RuntimeException("Missing required fields");
        }

        User user = userRepository.findByEmailIgnoreCase(identifier.trim())
                .orElseGet(() -> userRepository.findByUsernameIgnoreCase(identifier.trim()).orElse(null));

        if (user == null) {
            throw new RuntimeException("Account not found");
        }

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp.trim())) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        userRepository.save(user);

        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .operation("PASSWORD_RESET")
                .details("User successfully reset their password via OTP")
                .build());

        return user;
    }

    @DeleteMapping("/{id}")
    public Integer destroy(@PathVariable Long id) {
        userRepository.deleteById(id);
        return (int) (long) id;
    }
}
