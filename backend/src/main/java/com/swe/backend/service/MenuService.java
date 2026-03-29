package com.swe.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.model.MenuItemDto;
import com.swe.backend.repository.MenuRepository;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class MenuService {
    private final MenuRepository menuRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<MenuItemDto> getMenu() {
        return menuRepository.findActive();
    }

    public MenuItemDto add(MenuItemDto item) {
        menuRepository.insert(item);
        return menuRepository.findActiveByCode(item.code())
            .orElseThrow(() -> new IllegalStateException("Menu item was not created"));
    }

    public MenuItemDto updatePrice(String code, double newPrice) {
        boolean updated = menuRepository.updatePrice(code, newPrice);
        if (!updated) {
            throw new ResponseStatusException(NOT_FOUND, "Active item not found");
        }

        return menuRepository.findActiveByCode(code)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Active item not found"));
    }

    public void delete(String code) {
        boolean deleted = menuRepository.deactivate(code);
        if (!deleted) {
            throw new ResponseStatusException(NOT_FOUND, "Active item not found");
        }
    }

    public MenuItemDto findActive(String code) {
        return menuRepository.findActiveByCode(code)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Active item not found"));
    }
}
