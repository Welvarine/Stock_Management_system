package com.bnr.stockmanagement.repository;

import com.bnr.stockmanagement.entity.RoleChangeRequest;
import com.bnr.stockmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleChangeRequestRepository extends JpaRepository<RoleChangeRequest, Long> {
    List<RoleChangeRequest> findByUser(User user);
    List<RoleChangeRequest> findByStatus(RoleChangeRequest.RequestStatus status);
    Optional<RoleChangeRequest> findByUserAndStatus(User user, RoleChangeRequest.RequestStatus status);
    
    // For non-hidden records
    List<RoleChangeRequest> findByUserAndHiddenFalse(User user);
    List<RoleChangeRequest> findByStatusAndHiddenFalse(RoleChangeRequest.RequestStatus status);
    Optional<RoleChangeRequest> findByUserAndStatusAndHiddenFalse(User user, RoleChangeRequest.RequestStatus status);
    List<RoleChangeRequest> findAllByHiddenFalse();
}
