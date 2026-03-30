package com.swe.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swe.backend.model.InventoryItemDto;
import com.swe.backend.model.IssueResultDto;
import com.swe.backend.model.request.CreateInventoryItemRequest;
import com.swe.backend.model.request.QuantityRequest;
import com.swe.backend.service.InventoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryItemDto> list() {
        return inventoryService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)

    // POST /api/inventory: registers new item
    public InventoryItemDto addItem(@Valid @RequestBody CreateInventoryItemRequest request) {
        return inventoryService.add(new InventoryItemDto(
            request.code().trim(),
            request.name().trim(),
            request.unit().trim(),
            request.quantityOnHand(),
            0
        ));
    }

    // POST /{code}/receive: adds stock quantity   
    @PostMapping("/{code}/receive")
    public InventoryItemDto receive(@PathVariable String code, @Valid @RequestBody QuantityRequest request) {
        return inventoryService.receive(code, request.quantity());
    }

    // POST /{code}/issue: deducts stock quantity
    @PostMapping("/{code}/issue")
    public IssueResultDto issue(@PathVariable String code, @Valid @RequestBody QuantityRequest request) {
        return inventoryService.issue(code, request.quantity());
    }
}
