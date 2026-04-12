package com.swe.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.swe.backend.model.DashboardReportDto;
import com.swe.backend.model.ExpenseSummaryDto;
import com.swe.backend.model.InventoryTrendDto;
import com.swe.backend.model.ItemPerformanceDto;
import com.swe.backend.model.LiquidityStatusDto;
import com.swe.backend.model.SalesSummaryDto;
import com.swe.backend.service.ReportsService;

@WebMvcTest(controllers = ReportsController.class)
class ReportsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportsService reportsService;

    @Test
    void dashboard_returnsReport() throws Exception {
        DashboardReportDto report = new DashboardReportDto(
            List.of(new SalesSummaryDto("2026-04", 10, 12000.0)),
            List.of(new ItemPerformanceDto("FD1", "Fried Dumplings", 50, 3000.0)),
            new ExpenseSummaryDto(3, 5000.0, 2000.0, 3000.0),
            List.of(new InventoryTrendDto("RICE", "Rice", 4.0, 2.0, true)),
            new LiquidityStatusDto(45000.0, 1, 5000.0)
        );

        when(reportsService.dashboard(null, null)).thenReturn(report);

        mockMvc.perform(get("/api/reports/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.salesSummaries[0].month").value("2026-04"))
            .andExpect(jsonPath("$.liquidity.cashBalance").value(45000.0));
    }
}
