package com.bnr.stockmanagement.repository;

import com.bnr.stockmanagement.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByOrderByIdDesc();
    List<Request> findAllByStatusOrderByIdDesc(Request.Status status);
    List<Request> findAllByRequesterNameOrderByIdDesc(String requesterName);
}

