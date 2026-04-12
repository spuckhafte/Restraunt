package com.swe.backend.model;

import java.util.List;

public record DashboardReportDto(
    List<SalesSummaryDto> salesSummaries,
    List<ItemPerformanceDto> itemPerformance,
    ExpenseSummaryDto expenseSummary,
    List<InventoryTrendDto> inventoryTrends,
    LiquidityStatusDto liquidity
) {
}
