package com.swe.backend.service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.swe.backend.model.DashboardReportDto;
import com.swe.backend.repository.ReportsRepository;

@Service
public class ReportsService {
    private final ReportsRepository reportsRepository;

    public ReportsService(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    public DashboardReportDto dashboard(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from date cannot be after to date");
        }

        DashboardReportDto report = new DashboardReportDto(
            reportsRepository.salesSummaries(from, to),
            reportsRepository.itemPerformance(from, to),
            reportsRepository.expenses(from, to),
            reportsRepository.inventoryTrends(),
            reportsRepository.liquidity()
        );

        boolean hasNoData = report.salesSummaries().isEmpty()
            && report.itemPerformance().isEmpty()
            && report.expenseSummary().invoiceCount() == 0;

        if (hasNoData && from != null && to != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No data available for selected date range");
        }

        return report;
    }
}
