"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { ApiError, DashboardReportDto, ReportsApi } from "@/lib/api";

function isoDate(daysOffset: number): string {
  const date = new Date();
  date.setDate(date.getDate() + daysOffset);
  return date.toISOString().slice(0, 10);
}

function money(value: number): string {
  return `$${value.toFixed(2)}`;
}

export default function ReportsPanel() {
  const [from, setFrom] = useState<string>(() => isoDate(-30));
  const [to, setTo] = useState<string>(() => isoDate(0));
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [report, setReport] = useState<DashboardReportDto | null>(null);

  const loadReport = useCallback(async (fromValue?: string, toValue?: string) => {
    try {
      setLoading(true);
      setError(null);
      const data = await ReportsApi.dashboard(fromValue, toValue);
      setReport(data);
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to load dashboard report.");
      }
      setReport(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadReport(from, to);
  }, [from, to, loadReport]);

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    loadReport(from || undefined, to || undefined);
  };

  const totals = useMemo(() => {
    if (!report) {
      return {
        totalSales: 0,
        totalBills: 0,
      };
    }

    return report.salesSummaries.reduce(
      (acc, summary) => ({
        totalSales: acc.totalSales + summary.totalSales,
        totalBills: acc.totalBills + summary.billsCount,
      }),
      { totalSales: 0, totalBills: 0 }
    );
  }, [report]);

  return (
    <section className="rounded-2xl border border-white/10 bg-black/20 p-5 sm:p-6 space-y-5">
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <div>
          <h3 className="text-2xl text-emerald-300">Reporting Dashboard</h3>
          <p className="text-sm text-white/60">UC-04: Sales, item performance, expenses, inventory trend flags, and liquidity.</p>
        </div>

        <form onSubmit={onSubmit} className="flex flex-wrap items-end gap-3">
          <label className="text-xs text-white/60 uppercase tracking-widest">
            From
            <input
              type="date"
              value={from}
              onChange={(event) => setFrom(event.target.value)}
              className="block mt-1 rounded-lg border border-white/15 bg-white/[0.04] px-3 py-2 text-sm"
            />
          </label>
          <label className="text-xs text-white/60 uppercase tracking-widest">
            To
            <input
              type="date"
              value={to}
              onChange={(event) => setTo(event.target.value)}
              className="block mt-1 rounded-lg border border-white/15 bg-white/[0.04] px-3 py-2 text-sm"
            />
          </label>
          <button
            type="submit"
            disabled={loading}
            className="rounded-lg border border-emerald-400/50 bg-emerald-500/20 text-emerald-200 px-4 py-2 text-sm hover:bg-emerald-500/30 transition disabled:opacity-50"
          >
            {loading ? "Loading..." : "Refresh"}
          </button>
        </form>
      </div>

      {error && <p className="rounded-lg border border-red-400/30 bg-red-900/20 px-4 py-3 text-sm text-red-200">{error}</p>}

      {report && (
        <>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
            <Metric label="Sales" value={money(totals.totalSales)} />
            <Metric label="Bills" value={totals.totalBills.toString()} />
            <Metric label="Expenses" value={money(report.expenseSummary.totalExpenses)} />
            <Metric label="Cash Balance" value={money(report.liquidity.cashBalance)} />
          </div>

          <div className="grid lg:grid-cols-2 gap-4">
            <article className="rounded-xl border border-white/10 bg-white/[0.02] p-4">
              <h4 className="text-sm uppercase tracking-widest text-white/60 mb-3">Monthly Sales</h4>
              <div className="space-y-2 max-h-56 overflow-auto pr-1">
                {report.salesSummaries.map((summary) => (
                  <div key={summary.month} className="flex items-center justify-between text-sm border-b border-white/5 pb-2">
                    <span className="text-white/70">{summary.month}</span>
                    <span className="text-white">{summary.billsCount} bills</span>
                    <span className="text-emerald-300">{money(summary.totalSales)}</span>
                  </div>
                ))}
                {report.salesSummaries.length === 0 && <p className="text-sm text-white/45">No sales in selected period.</p>}
              </div>
            </article>

            <article className="rounded-xl border border-white/10 bg-white/[0.02] p-4">
              <h4 className="text-sm uppercase tracking-widest text-white/60 mb-3">Top Item Performance</h4>
              <div className="space-y-2 max-h-56 overflow-auto pr-1">
                {report.itemPerformance.map((item) => (
                  <div key={`${item.itemCode}-${item.itemName}`} className="text-sm border-b border-white/5 pb-2">
                    <p className="text-white">{item.itemName} <span className="text-white/50">({item.itemCode})</span></p>
                    <p className="text-white/60">{item.quantitySold} sold • {money(item.revenue)}</p>
                  </div>
                ))}
                {report.itemPerformance.length === 0 && <p className="text-sm text-white/45">No item performance data.</p>}
              </div>
            </article>
          </div>

          <div className="grid lg:grid-cols-2 gap-4">
            <article className="rounded-xl border border-white/10 bg-white/[0.02] p-4">
              <h4 className="text-sm uppercase tracking-widest text-white/60 mb-3">Expense Breakdown</h4>
              <ul className="space-y-1.5 text-sm">
                <li className="flex justify-between"><span className="text-white/65">Invoice count</span><span>{report.expenseSummary.invoiceCount}</span></li>
                <li className="flex justify-between"><span className="text-white/65">Total expenses</span><span className="text-rose-300">{money(report.expenseSummary.totalExpenses)}</span></li>
                <li className="flex justify-between"><span className="text-white/65">Paid expenses</span><span>{money(report.expenseSummary.paidExpenses)}</span></li>
                <li className="flex justify-between"><span className="text-white/65">Unpaid expenses</span><span>{money(report.expenseSummary.unpaidExpenses)}</span></li>
              </ul>
            </article>

            <article className="rounded-xl border border-white/10 bg-white/[0.02] p-4">
              <h4 className="text-sm uppercase tracking-widest text-white/60 mb-3">Inventory Trend Flags</h4>
              <div className="space-y-2 max-h-56 overflow-auto pr-1">
                {report.inventoryTrends.map((trend) => (
                  <div key={trend.itemCode} className="text-sm border-b border-white/5 pb-2">
                    <p className="text-white">{trend.itemName} <span className="text-white/50">({trend.itemCode})</span></p>
                    <p className="text-white/60">Today: {trend.issuedToday} • 3-day avg: {trend.threeDayAverage.toFixed(2)}</p>
                    {trend.flaggedUnusual && <p className="text-amber-300 text-xs uppercase tracking-wider">Flagged unusual usage</p>}
                  </div>
                ))}
                {report.inventoryTrends.length === 0 && <p className="text-sm text-white/45">No trend data available.</p>}
              </div>
            </article>
          </div>
        </>
      )}
    </section>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <article className="rounded-xl border border-white/10 bg-white/[0.04] p-4">
      <p className="text-xs uppercase tracking-widest text-white/55 mb-2">{label}</p>
      <p className="text-xl text-white">{value}</p>
    </article>
  );
}
