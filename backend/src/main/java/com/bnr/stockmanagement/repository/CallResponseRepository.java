package com.bnr.stockmanagement.repository;

import com.bnr.stockmanagement.entity.CallResponse;
import com.bnr.stockmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallResponseRepository extends JpaRepository<CallResponse, Long> {
    List<CallResponse> findAllByUserOrderByCreatedAtDesc(User user);
    List<CallResponse> findAllByOrderByCreatedAtDesc();
    
    // For non-hidden records
    List<CallResponse> findAllByHiddenFalseOrderByCreatedAtDesc();
    List<CallResponse> findAllByUserAndHiddenFalseOrderByCreatedAtDesc(User user);
}
