package com.swe.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.model.InventoryItemDto;
import com.swe.backend.model.IssueResultDto;
import com.swe.backend.model.SupplierInvoiceDto;
import com.swe.backend.repository.InventoryRepository;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<InventoryItemDto> list() {
        return inventoryRepository.list();
    }

    public InventoryItemDto add(InventoryItemDto item) {
        inventoryRepository.insert(item);
        return inventoryRepository.findByCode(item.code())
            .orElseThrow(() -> new IllegalStateException("Inventory item was not created"));
    }

    // Delegates stock increase to repository
    public InventoryItemDto receive(String code, double quantity) {
        try {
            return inventoryRepository.receive(code, quantity);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    // Delegates stock deduction; returns flag result
    public IssueResultDto issue(String code, double quantity) {
        try {
            return inventoryRepository.issue(code, quantity);
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() == null ? "Invalid inventory operation" : ex.getMessage();
            if ("Inventory item not found".equals(message)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    public SupplierInvoiceDto createInvoice(
        String supplierName,
        String itemCode,
        double quantity,
        double unitPrice,
        java.time.LocalDate invoiceDate,
        boolean approved,
        long createdByUserId
    ) {
        try {
            return inventoryRepository.createInvoiceAndReceive(
                supplierName,
                itemCode,
                quantity,
                unitPrice,
                invoiceDate,
                approved,
                createdByUserId
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    public List<SupplierInvoiceDto> listInvoices() {
        return inventoryRepository.listInvoices();
    }
}
