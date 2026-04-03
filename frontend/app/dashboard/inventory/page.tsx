"use client";

import React from "react";
import InventoryPanel from "@/components/dashboard/InventoryPanel";

export default function InventoryDashboard() {
  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-3xl font-light text-white tracking-widest uppercase mb-2">Warehouse <span className="font-bold text-fuchsia-400">Inventory</span></h1>
        <p className="text-white/40 font-mono text-sm">Access Level: Specialized // Sector: Logistics</p>
      </header>

      <InventoryPanel />
    </div>
  );
}
