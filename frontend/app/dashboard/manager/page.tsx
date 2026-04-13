"use client";

import React from "react";
import MenuPanel from "@/components/dashboard/MenuPanel";
import InventoryPanel from "@/components/dashboard/InventoryPanel";
import ReportsPanel from "@/components/dashboard/ReportsPanel";
import InvoicesPanel from "@/components/dashboard/InvoicesPanel";
import CheckGenerationPanel from "@/components/dashboard/CheckGenerationPanel";
import ManagerOverridePanel from "@/components/dashboard/ManagerOverridePanel";
import { getSession } from "@/lib/session";

export default function ManagerDashboard() {
  const session = getSession();
  const effectiveRole = session?.effectiveRole;

  const canManageMenu = effectiveRole === "MANAGER";
  const canUseInventory = effectiveRole === "MANAGER" || effectiveRole === "INVENTORY";
  const canViewManagerAnalytics = effectiveRole === "MANAGER";

  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-3xl font-light text-white tracking-widest uppercase mb-2">Operations <span className="font-bold text-emerald-300">Manager</span></h1>
        <p className="text-white/50 text-sm">Unified manager console for UC-01 through UC-06. Current effective role: {effectiveRole ?? "UNKNOWN"}.</p>
      </header>

      <ManagerOverridePanel />

      {canViewManagerAnalytics ? (
        <ReportsPanel />
      ) : (
        <div className="rounded-2xl border border-amber-400/30 bg-amber-900/20 p-5 text-sm text-amber-100">
          Reports and check generation require effective role MANAGER. Use override restore to regain full manager access.
        </div>
      )}

      {canUseInventory ? <InvoicesPanel /> : null}
      {canViewManagerAnalytics ? <CheckGenerationPanel /> : null}

      <div className="grid lg:grid-cols-2 gap-8 pt-4 border-t border-white/5">
        <div className="w-full overflow-hidden">
          <MenuPanel readOnly={!canManageMenu} />
        </div>
        <div className="w-full overflow-hidden">
          <InventoryPanel readOnly={!canUseInventory} />
        </div>
      </div>
    </div>
  );
}
