package com.swe.backend.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.model.BillDto;
import com.swe.backend.model.SaleLineDto;
import com.swe.backend.service.SalesService;

@WebMvcTest(controllers = SalesController.class)
class SalesControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalesService salesService;

    @Test
    void processSale_returnsCreatedBill() throws Exception {
        List<SaleLineDto> lines = List.of(new SaleLineDto("FD1", "Fried Dumplings", 60.0, 2, 120.0));
        when(salesService.processSale(anyList()))
            .thenReturn(new BillDto(1L, lines, 120.0, Instant.now(), false));

        mockMvc.perform(post("/api/sales")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "entries": [
                        {
                          "itemCode": "FD1",
                          "quantity": 2
                        }
                      ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.subtotal").value(120.0));
    }

    @Test
    void voidSale_returnsUpdatedBill() throws Exception {
        when(salesService.voidSale(1L))
            .thenReturn(new BillDto(1L, List.of(), 120.0, Instant.now(), true));

        mockMvc.perform(post("/api/sales/1/void"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.voided").value(true));
    }

        @Test
        void processSale_emptyEntries_returnsBadRequest() throws Exception {
          mockMvc.perform(post("/api/sales")
              .contentType(MediaType.APPLICATION_JSON)
              .content("{\"entries\":[]}"))
            .andExpect(status().isBadRequest());
        }

        @Test
        void voidSale_notFound_returnsNotFound() throws Exception {
          when(salesService.voidSale(99L))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found or already voided"));

          mockMvc.perform(post("/api/sales/99/void"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Bill not found or already voided"));
        }
}
