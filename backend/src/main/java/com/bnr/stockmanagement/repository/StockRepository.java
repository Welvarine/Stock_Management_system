package com.bnr.stockmanagement.repository;

import com.bnr.stockmanagement.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("SELECT s FROM Stock s WHERE s.deleted = false OR s.deleted IS NULL ORDER BY s.id DESC")
    List<Stock> findAllActiveOrderByIdDesc();

    @Query("SELECT s FROM Stock s WHERE s.deleted = true ORDER BY s.deletedAt DESC")
    List<Stock> findAllDeletedOrderByDeletedAtDesc();
}
