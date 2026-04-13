"use client";

import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { ApiError, InventoryApi, InventoryItemDto, SupplierInvoiceDto } from "@/lib/api";

function money(value: number): string {
  return `$${value.toFixed(2)}`;
}

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

export default function InvoicesPanel() {
  const [invoices, setInvoices] = useState<SupplierInvoiceDto[]>([]);
  const [items, setItems] = useState<InventoryItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [supplierName, setSupplierName] = useState("");
  const [itemCode, setItemCode] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [unitPrice, setUnitPrice] = useState("0");
  const [invoiceDate, setInvoiceDate] = useState(todayIso());
  const [approved, setApproved] = useState(true);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const [invoiceData, itemData] = await Promise.all([
        InventoryApi.listInvoices(),
        InventoryApi.list(),
      ]);
      setInvoices(invoiceData);
      setItems(itemData);
      setItemCode((previous) => previous || itemData[0]?.code || "");
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to load invoices.");
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const unpaidApprovedInvoices = useMemo(
    () => invoices.filter((invoice) => invoice.approved && !invoice.paid),
    [invoices]
  );

  const handleCreate = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    try {
      setSubmitting(true);
      setError(null);

      await InventoryApi.createInvoice({
        supplierName: supplierName.trim(),
        itemCode,
        quantity: Number(quantity),
        unitPrice: Number(unitPrice),
        invoiceDate,
        approved,
      });

      setSupplierName("");
      setQuantity("1");
      setUnitPrice("0");
      setInvoiceDate(todayIso());
      setApproved(true);
      await loadData();
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to create supplier invoice.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="rounded-2xl border border-white/10 bg-black/20 p-5 sm:p-6 space-y-5">
      <div>
        <h3 className="text-2xl text-cyan-300">Supplier Invoices</h3>
        <p className="text-sm text-white/60">UC-03: Register supplier invoices and auto-receive approved stock.</p>
      </div>

      {error && <p className="rounded-lg border border-red-400/30 bg-red-900/20 px-4 py-3 text-sm text-red-200">{error}</p>}

      <form onSubmit={handleCreate} className="grid md:grid-cols-2 xl:grid-cols-6 gap-3 rounded-xl border border-white/10 bg-white/[0.03] p-4">
        <input
          type="text"
          required
          value={supplierName}
          onChange={(event) => setSupplierName(event.target.value)}
          placeholder="Supplier name"
          className="xl:col-span-2 rounded-lg border border-white/15 bg-black/20 px-3 py-2 text-sm"
        />

        <select
          value={itemCode}
          onChange={(event) => setItemCode(event.target.value)}
          className="rounded-lg border border-white/15 bg-black/20 px-3 py-2 text-sm"
          required
        >
          {items.map((item) => (
            <option key={item.code} value={item.code}>
              {item.code} - {item.name}
            </option>
          ))}
        </select>

        <input
          type="number"
          min="0.01"
          step="0.01"
          value={quantity}
          onChange={(event) => setQuantity(event.target.value)}
          className="rounded-lg border border-white/15 bg-black/20 px-3 py-2 text-sm"
          required
        />

        <input
          type="number"
          min="0.01"
          step="0.01"
          value={unitPrice}
          onChange={(event) => setUnitPrice(event.target.value)}
          className="rounded-lg border border-white/15 bg-black/20 px-3 py-2 text-sm"
          required
        />

        <input
          type="date"
          value={invoiceDate}
          onChange={(event) => setInvoiceDate(event.target.value)}
          className="rounded-lg border border-white/15 bg-black/20 px-3 py-2 text-sm"
          required
        />

        <label className="flex items-center gap-2 text-sm text-white/80">
          <input type="checkbox" checked={approved} onChange={(event) => setApproved(event.target.checked)} />
          Approved
        </label>

        <button
          type="submit"
          disabled={submitting || loading || items.length === 0}
          className="md:col-span-2 xl:col-span-1 rounded-lg bg-cyan-500 text-black font-semibold px-4 py-2.5 hover:bg-cyan-400 transition disabled:opacity-50"
        >
          {submitting ? "Saving..." : "Create Invoice"}
        </button>
      </form>

      <div className="rounded-xl border border-white/10 overflow-hidden">
        <div className="max-h-[420px] overflow-auto">
          <table className="w-full text-sm">
            <thead className="bg-white/[0.04] sticky top-0">
              <tr className="text-left text-white/65 uppercase tracking-widest text-xs">
                <th className="px-3 py-2">ID</th>
                <th className="px-3 py-2">Supplier</th>
                <th className="px-3 py-2">Item</th>
                <th className="px-3 py-2">Total</th>
                <th className="px-3 py-2">Date</th>
                <th className="px-3 py-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {[...invoices]
                .sort((a, b) => b.id - a.id)
                .map((invoice) => (
                  <tr key={invoice.id} className="border-t border-white/5">
                    <td className="px-3 py-2">#{invoice.id}</td>
                    <td className="px-3 py-2">{invoice.supplierName}</td>
                    <td className="px-3 py-2">{invoice.itemCode} ({invoice.quantity})</td>
                    <td className="px-3 py-2 text-cyan-300">{money(invoice.totalAmount)}</td>
                    <td className="px-3 py-2">{invoice.invoiceDate}</td>
                    <td className="px-3 py-2">
                      <span className={`text-xs px-2 py-1 rounded ${invoice.paid ? "bg-emerald-500/20 text-emerald-200" : "bg-amber-500/20 text-amber-200"}`}>
                        {invoice.paid ? "Paid" : "Unpaid"}
                      </span>
                      {!invoice.approved && <span className="text-xs ml-2 text-rose-300">Not approved</span>}
                      {invoice.flaggedForReview && <span className="text-xs ml-2 text-amber-300">Flagged</span>}
                    </td>
                  </tr>
                ))}
              {!loading && invoices.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-3 py-6 text-center text-white/50">No supplier invoices yet.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <p className="text-xs text-white/60">
        Ready for payment: <span className="text-cyan-300">{unpaidApprovedInvoices.length}</span> approved unpaid invoice(s).
      </p>
    </section>
  );
}
