package com.bnr.stockmanagement.controller;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnr.stockmanagement.entity.Stock;
import com.bnr.stockmanagement.repository.StockRepository;

@RestController
@RequestMapping("/stocks")
@CrossOrigin(origins = "http://localhost:5173")

public class StockController {

    @Autowired
    private StockRepository stockRepository;
    
    @GetMapping("/")
    public List<Stock> findAll() {
        return stockRepository.findAllActiveOrderByIdDesc();
    }

    @GetMapping("/deleted")
    public List<Stock> findAllDeleted() {
        return stockRepository.findAllDeletedOrderByDeletedAtDesc();
    }

    @GetMapping("/{id}")
    public Stock findOne(@PathVariable Long id) {
        Stock stock = stockRepository.findById(id).orElse(null);
        if (stock == null || Boolean.TRUE.equals(stock.getDeleted())) return null;
        return stock;
    }
// public Stock update(@PathVariable Long id, @RequestBody Stock stock)
    @PostMapping("/")
    // Create a new stock Post request with JSON body containing stock details
   public Stock create(@RequestBody Stock stock) {
        stock.setDeleted(false);
        stock.setDeletedAt(null);
        if (stock.getInitialQuantity() == null) stock.setInitialQuantity(stock.getQuantity());
        return stockRepository.save(stock);
    }
    @PutMapping("/{id}")
    public Stock update(@PathVariable Long id, @RequestBody Stock stock) {
        Stock oldStock = stockRepository.findById(id).orElse(null);
        if (oldStock != null && !Boolean.TRUE.equals(oldStock.getDeleted())) {
            oldStock.setName(stock.getName());
            oldStock.setCategory(stock.getCategory());
            oldStock.setQuantity(stock.getQuantity());
            oldStock.setPrice(stock.getPrice());
            oldStock.setStatus(stock.getStatus());
            if (stock.getInitialQuantity() != null) oldStock.setInitialQuantity(stock.getInitialQuantity());
            return stockRepository.save(oldStock);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Stock stock = stockRepository.findById(id).orElse(null);
        if (stock == null || Boolean.TRUE.equals(stock.getDeleted())) return;
        stock.setDeleted(true);
        stock.setDeletedAt(LocalDateTime.now());
        stockRepository.save(stock);
    }

    @PostMapping("/{id}/restore")
    public Stock restore(@PathVariable Long id) {
        Stock stock = stockRepository.findById(id).orElse(null);
        if (stock == null || !Boolean.TRUE.equals(stock.getDeleted())) return null;
        stock.setDeleted(false);
        stock.setDeletedAt(null);
        return stockRepository.save(stock);
    }
}

/*package com.bnr.stockmanagement.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bnr.stockmanagement.entity.Stock;
import com.bnr.stockmanagement.repository.StockRepository;

@RestController  // ← ADD THIS
@RequestMapping("/stocks")  // ← ADD THIS to map base path
public class StockController {

    @Autowired
    private StockRepository stockRepository;
    
    @GetMapping("/")
    public List<Stock> findAll() {  // ← Add 'public'
        return stockRepository.findAll();
    }

    @GetMapping("/{id}")
    public Stock findOne(@PathVariable Long id) {  // ← Add 'public'
        return stockRepository.findById(id).orElse(null);
    }

    @PostMapping("/")
    public Stock create(@RequestBody Stock stock) {  // ← Add 'public'
        return stockRepository.save(stock);
    }

    @PutMapping("/{id}")
    public Stock update(@PathVariable Long id, @RequestBody Stock stock) {  // ← Add 'public'
        Stock oldStock = stockRepository.findById(id).orElse(null);
        if (oldStock != null) {  // ← Add null check to prevent NullPointerException
            oldStock.setName(stock.getName());
            oldStock.setQuantity(stock.getQuantity());
            oldStock.setPrice(stock.getPrice());
            return stockRepository.save(oldStock);
        }
        return null;  // or throw an exception like: throw new RuntimeException("Stock not found");
    }
}*/