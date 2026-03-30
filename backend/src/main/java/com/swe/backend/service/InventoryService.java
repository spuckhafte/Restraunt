package com.swe.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.swe.backend.model.InventoryItemDto;
import com.swe.backend.model.IssueResultDto;
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
        return inventoryRepository.receive(code, quantity);
    }

    // Delegates stock deduction; returns flag result
    public IssueResultDto issue(String code, double quantity) {
        return inventoryRepository.issue(code, quantity);
    }
}
