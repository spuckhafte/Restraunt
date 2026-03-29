package com.swe.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import com.swe.backend.model.InventoryItemDto;
import com.swe.backend.model.IssueResultDto;
import com.swe.backend.service.InventoryService;

@WebMvcTest(controllers = InventoryController.class)
class InventoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void list_returnsItems() throws Exception {
        when(inventoryService.list()).thenReturn(List.of(new InventoryItemDto("RICE", "Rice", "kg", 20.0, 2.0)));

        mockMvc.perform(get("/api/inventory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("RICE"));
    }

    @Test
    void addItem_returnsCreated() throws Exception {
        when(inventoryService.add(any(InventoryItemDto.class)))
            .thenReturn(new InventoryItemDto("FLOUR", "Flour", "kg", 30.0, 0));

        mockMvc.perform(post("/api/inventory")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "FLOUR",
                      "name": "Flour",
                      "unit": "kg",
                      "quantityOnHand": 30.0
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("FLOUR"));
    }

    @Test
    void receive_returnsUpdatedItem() throws Exception {
        when(inventoryService.receive("RICE", 5.0))
            .thenReturn(new InventoryItemDto("RICE", "Rice", "kg", 25.0, 2.0));

        mockMvc.perform(post("/api/inventory/RICE/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":5.0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantityOnHand").value(25.0));
    }

    @Test
    void issue_returnsIssueResult() throws Exception {
        InventoryItemDto item = new InventoryItemDto("RICE", "Rice", "kg", 18.0, 4.0);
        when(inventoryService.issue("RICE", 2.0)).thenReturn(new IssueResultDto(item, false));

        mockMvc.perform(post("/api/inventory/RICE/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":2.0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flaggedUnusualConsumption").value(false));
    }

    @Test
    void receive_invalidQuantity_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/inventory/RICE/receive")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":0}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void issue_notFound_returnsNotFound() throws Exception {
        when(inventoryService.issue("MISSING", 1.0))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Inventory item not found"));

        mockMvc.perform(post("/api/inventory/MISSING/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":1.0}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Inventory item not found"));
    }
}
