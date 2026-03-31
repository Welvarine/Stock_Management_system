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
        return requestRepository.save(req);
    }

    @PutMapping("/{id}/status")
    public Request updateStatus(@PathVariable Long id, @RequestBody StatusUpdate body) {
        Request req = requestRepository.findById(id).orElse(null);
        if (req == null) return null;
        req.setStatus(body.status);
        if (body.rejectionReason != null) req.setRejectionReason(body.rejectionReason);
        return requestRepository.save(req);
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

