package com.bnr.stockmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemDTO {
    private Long id;

    @NotBlank(message = "Item name is required")
    private String name;

    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;

    @DecimalMin(value = "0.0", message = "Price must be non-negative")
    private BigDecimal price;

    private String status;
}