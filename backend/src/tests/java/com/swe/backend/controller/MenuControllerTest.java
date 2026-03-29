package com.swe.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import com.swe.backend.model.MenuItemDto;
import com.swe.backend.service.MenuService;

@WebMvcTest(controllers = MenuController.class)
class MenuControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @Test
    void getMenu_returnsItems() throws Exception {
        when(menuService.getMenu()).thenReturn(List.of(new MenuItemDto("FD1", "Fried Dumplings", 60.0, true)));

        mockMvc.perform(get("/api/menu"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("FD1"));
    }

    @Test
    void addItem_returnsCreated() throws Exception {
        when(menuService.add(any(MenuItemDto.class))).thenReturn(new MenuItemDto("NV1", "Noodles Veg", 75.0, true));

        mockMvc.perform(post("/api/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "NV1",
                      "name": "Noodles Veg",
                      "basePrice": 75.0
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("NV1"));
    }

    @Test
    void updatePrice_returnsUpdatedItem() throws Exception {
        when(menuService.updatePrice("FD1", 99.0)).thenReturn(new MenuItemDto("FD1", "Fried Dumplings", 99.0, true));

        mockMvc.perform(put("/api/menu/FD1/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPrice\":99.0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.basePrice").value(99.0));
    }

    @Test
    void deleteItem_returnsOkMessage() throws Exception {
        doNothing().when(menuService).delete("FD1");

        mockMvc.perform(delete("/api/menu/FD1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Item deactivated"));
    }

    @Test
    void addItem_negativePrice_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "BAD1",
                      "name": "Bad Item",
                      "basePrice": -1.0
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updatePrice_notFound_returnsNotFound() throws Exception {
        when(menuService.updatePrice("MISSING", 99.0))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Active item not found"));

        mockMvc.perform(put("/api/menu/MISSING/price")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPrice\":99.0}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Active item not found"));
    }

    @Test
    void deleteItem_notFound_returnsNotFound() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Active item not found"))
            .when(menuService).delete("MISSING");

        mockMvc.perform(delete("/api/menu/MISSING"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Active item not found"));
    }
}
