package com.swe.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.model.BillDto;
import com.swe.backend.model.MenuItemDto;
import com.swe.backend.model.SaleLineDto;
import com.swe.backend.model.request.SaleEntryRequest;
import com.swe.backend.repository.SalesRepository;

@Service
public class SalesService {
    private final MenuService menuService;
    private final SalesRepository salesRepository;

    public SalesService(MenuService menuService, SalesRepository salesRepository) {
        this.menuService = menuService;
        this.salesRepository = salesRepository;
    }

    public BillDto processSale(List<SaleEntryRequest> entries) {
        if (entries == null || entries.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No sale entries provided");
        }

        List<SaleLineDto> lines = new ArrayList<>();
        double subtotal = 0;

        for (SaleEntryRequest entry : entries) {
            if (entry.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
            }

            MenuItemDto item = menuService.findActive(entry.itemCode());
            double lineTotal = item.basePrice() * entry.quantity();

            lines.add(new SaleLineDto(
                item.code(),
                item.name(),
                item.basePrice(),
                entry.quantity(),
                lineTotal
            ));
            subtotal += lineTotal;
        }

        long billId = salesRepository.createBill(subtotal, lines);
        return salesRepository.findBill(billId)
            .orElseThrow(() -> new IllegalStateException("Bill was not created"));
    }

    public BillDto voidSale(long billId) {
        boolean voided = salesRepository.voidBill(billId);
        if (!voided) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found or already voided");
        }

        return salesRepository.findBill(billId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
    }
}
