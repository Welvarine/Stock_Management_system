package com.bnr.stockmanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDTO {
    private Long id;

    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotBlank(message = "Requester name is required")
    private String requesterName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String status;
}