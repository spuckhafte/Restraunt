package com.swe.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swe.backend.model.BillDto;
import com.swe.backend.model.request.CreateSaleRequest;
import com.swe.backend.service.SalesService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sales")
public class SalesController {
    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)

    // POST /api/sales: creates a new bill
    public BillDto processSale(@Valid @RequestBody CreateSaleRequest request) {
        return salesService.processSale(request.entries());
    }

    // POST /api/sales/{id}/void — cancels a bill
    @PostMapping("/{billId}/void")
    public BillDto voidSale(@PathVariable long billId) {
        return salesService.voidSale(billId);
    }
}
