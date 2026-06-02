package com.bnr.stockmanagement.controller;

import com.bnr.stockmanagement.entity.Request;
import com.bnr.stockmanagement.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/requests")
@CrossOrigin(origins = "http://localhost:5173")
public class RequestController {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private com.bnr.stockmanagement.repository.UserRepository userRepository;

    @Autowired
    private com.bnr.stockmanagement.repository.AuditLogRepository auditLogRepository;

    @GetMapping("/")
    public List<Request> findAll(
            @RequestParam(required = false) String requesterName,
            @RequestParam(required = false) Request.Status status
    ) {
        if (requesterName != null && !requesterName.isBlank()) {
            return requestRepository.findAllByRequesterNameOrderByIdDesc(requesterName);
        }
        if (status != null) {
            return requestRepository.findAllByStatusOrderByIdDesc(status);
        }
        return requestRepository.findAllByOrderByIdDesc();
    }

    @PostMapping("/")
    public Request create(@RequestBody Request req) {
        req.setId(null);
        if (req.getStatus() == null) req.setStatus(Request.Status.Pending);
        Request savedReq = requestRepository.save(req);

        userRepository.findByUsername(req.getRequesterName()).ifPresent(user -> {
            // here the operation is called from the frontend 
            
            //Then it is saved in the audit log table through the userRepository findByUsername method 
            //or the user ID could be passed from the frontend and saved in the audit log table
            //the operations displayed in the audit log table are: 
            //STOCK_REQUEST_CREATED
            //STOCK_REQUEST_STATUS_CHANGED
            //STOCK_REQUEST_DELETED
            //STOCK_REQUEST_UPDATED
            //Then it is displayed in the audit log table, using the findAll() method in the audit log controller
            //To view all requests, use the findAll() method in the request controller

            auditLogRepository.save(com.bnr.stockmanagement.entity.AuditLog.builder()
             .user(user)
                    .operation("STOCK_REQUEST_CREATED")
                    .details("Requested " + req.getQuantity() + "x " + req.getItemName() + " (ID: " + savedReq.getId() + ")")
                    .build());
        });
       

        return savedReq;
    }

    @PutMapping("/{id}/status")
    public Request updateStatus(@PathVariable Long id, @RequestBody StatusUpdate body) {
        Request req = requestRepository.findById(id).orElse(null);
        if (req == null) return null;
        req.setStatus(body.status);
        if (body.rejectionReason != null) req.setRejectionReason(body.rejectionReason);
        Request savedReq = requestRepository.save(req);

        // We don't have the current user here easily without security context, 
        // but we can log the action. For now, let's log it against the requester for visibility,
        // or better, we could pass the approver name in the body.
        userRepository.findByUsername(req.getRequesterName()).ifPresent(user -> {
            auditLogRepository.save(com.bnr.stockmanagement.entity.AuditLog.builder()
                    .user(user)
                    .operation("STOCK_REQUEST_STATUS_CHANGED")
                    .details("Request " + id + " status changed to " + body.status + (body.rejectionReason != null ? ". Reason: " + body.rejectionReason : ""))
                    .build());
        });

        return savedReq;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        requestRepository.deleteById(id);
    }

    public static class StatusUpdate {
        public Request.Status status;
        public String rejectionReason;
        
    }
}

