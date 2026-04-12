package com.swe.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.swe.backend.model.MenuItemDto;
import com.swe.backend.model.SimpleMessage;
import com.swe.backend.model.request.CreateMenuItemRequest;
import com.swe.backend.model.request.UpdatePriceRequest;
import com.swe.backend.service.MenuService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menu")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuItemDto> getMenu() {
        return menuService.getMenu();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)

    // POST /api/menu: adds a new menu item
    public MenuItemDto addItem(@Valid @RequestBody CreateMenuItemRequest request) {
        return menuService.add(new MenuItemDto(
            request.code().trim(),
            request.name().trim(),
            request.basePrice(),
            true
        ));
    }

    @PutMapping("/{code}/price")
    // PUT /{code}/price: updates item price
    public MenuItemDto updatePrice(@PathVariable String code, @Valid @RequestBody UpdatePriceRequest request) {
        return menuService.updatePrice(code, request.newPrice());
    }

    @DeleteMapping("/{code}")
    // DELETE /{code}: soft-deletes a menu item
    public SimpleMessage deleteItem(@PathVariable String code) {
        menuService.delete(code);
        return new SimpleMessage("Item deactivated");
    }
}
