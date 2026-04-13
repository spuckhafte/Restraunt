"use client";

import { useEffect, useMemo, useState } from "react";
import { ApiError, CheckPaymentDto, InventoryApi, PaymentsApi, SupplierInvoiceDto } from "@/lib/api";

function money(value: number): string {
  return `$${value.toFixed(2)}`;
}

export default function CheckGenerationPanel() {
  const [invoices, setInvoices] = useState<SupplierInvoiceDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedInvoiceId, setSelectedInvoiceId] = useState<number | null>(null);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<CheckPaymentDto | null>(null);

  const loadInvoices = async () => {
    try {
      setLoading(true);
      const data = await InventoryApi.listInvoices();
      setInvoices(data);
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to load invoices.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInvoices();
  }, []);

  const payableInvoices = useMemo(
    () => invoices.filter((invoice) => invoice.approved && !invoice.paid),
    [invoices]
  );

  const selectedInvoice = useMemo(
    () => payableInvoices.find((invoice) => invoice.id === selectedInvoiceId) ?? null,
    [payableInvoices, selectedInvoiceId]
  );

  const handleGenerate = async () => {
    if (!selectedInvoiceId) {
      setError("Select an approved unpaid invoice first.");
      return;
    }

    try {
      setProcessing(true);
      setError(null);
      const generated = await PaymentsApi.generateCheck(selectedInvoiceId);
      setResult(generated);
      await loadInvoices();
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to generate supplier check.");
      }
    } finally {
      setProcessing(false);
    }
  };

  return (
    <section className="rounded-2xl border border-white/10 bg-black/20 p-5 sm:p-6 space-y-5">
      <div>
        <h3 className="text-2xl text-emerald-300">Supplier Check Generation</h3>
        <p className="text-sm text-white/60">UC-06: Generate PDF checks for approved unpaid supplier invoices.</p>
      </div>

      <div className="grid md:grid-cols-3 gap-3">
        <div className="md:col-span-2">
          <label className="block text-xs uppercase tracking-widest text-white/60 mb-2">Invoice</label>
          <select
            value={selectedInvoiceId ?? ""}
            onChange={(event) => {
              const next = event.target.value ? Number(event.target.value) : null;
              setSelectedInvoiceId(next);
            }}
            className="w-full rounded-lg border border-white/15 bg-white/[0.04] px-3 py-2 text-sm"
          >
            <option value="">Select invoice...</option>
            {payableInvoices.map((invoice) => (
              <option key={invoice.id} value={invoice.id}>
                #{invoice.id} - {invoice.supplierName} - {money(invoice.totalAmount)}
              </option>
            ))}
          </select>
        </div>

        <button
          type="button"
          onClick={handleGenerate}
          disabled={loading || processing || payableInvoices.length === 0}
          className="rounded-lg bg-emerald-500 text-black px-4 py-2.5 text-sm font-semibold hover:bg-emerald-400 transition disabled:opacity-50"
        >
          {processing ? "Generating..." : "Generate Check"}
        </button>
      </div>

      {selectedInvoice && (
        <p className="text-sm text-white/70">
          Selected invoice #{selectedInvoice.id} • {selectedInvoice.supplierName} • {money(selectedInvoice.totalAmount)}
        </p>
      )}

      {error && <p className="rounded-lg border border-red-400/30 bg-red-900/20 px-4 py-3 text-sm text-red-200">{error}</p>}

      {result && (
        <article className="rounded-xl border border-emerald-400/20 bg-emerald-500/10 p-4 space-y-2">
          <p className="text-sm uppercase tracking-wider text-emerald-200">Check Created</p>
          <p className="text-sm text-white/80">Invoice #{result.invoiceId} • Check #{result.checkNumber}</p>
          <p className="text-sm text-white/80">Amount {money(result.amount)} • New cash balance {money(result.cashBalanceAfterPayment)}</p>
          <a
            href={`data:application/pdf;base64,${result.pdfBase64}`}
            download={`check-${result.checkNumber}.pdf`}
            className="inline-block mt-1 rounded-lg border border-emerald-300/40 px-3 py-2 text-sm text-emerald-100 hover:bg-emerald-500/20 transition"
          >
            Download Check PDF
          </a>
        </article>
      )}

      {payableInvoices.length === 0 && (
        <p className="text-sm text-white/50">No approved unpaid invoices currently available for check generation.</p>
      )}
    </section>
  );
}
