"use client";

import React from "react";
import InventoryPanel from "@/components/dashboard/InventoryPanel";
import InvoicesPanel from "@/components/dashboard/InvoicesPanel";

export default function InventoryDashboard() {
  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-3xl font-light text-white tracking-widest uppercase mb-2">Warehouse <span className="font-bold text-fuchsia-400">Inventory</span></h1>
        <p className="text-white/50 text-sm">UC-03 operations: stock control, receiving, issuing, and supplier invoice intake.</p>
      </header>

      <InventoryPanel />
      <InvoicesPanel />
    </div>
  );
}
