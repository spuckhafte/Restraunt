"use client";

import React from "react";
import MenuPanel from "@/components/dashboard/MenuPanel";
import InventoryPanel from "@/components/dashboard/InventoryPanel";

export default function ManagerDashboard() {
  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-3xl font-light text-white tracking-widest uppercase mb-2">Ops <span className="font-bold text-cyan-400">Manager</span></h1>
        <p className="text-white/40 font-mono text-sm">Access Level: Elevated // Sector: Day-to-Day Operations</p>
      </header>

      {/* Placeholders for unimplemented APIs */}
      <div className="grid grid-cols-1 gap-4">
        <div className="bg-black/80 backdrop-blur-md border border-white/5 rounded-2xl p-6 relative overflow-hidden group">
          <div className="absolute inset-0 bg-gradient-to-r from-red-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
          <h3 className="text-white/70 tracking-widest uppercase mb-2 flex justify-between items-center">
            Staff & Workflow Data
            <span className="text-[10px] bg-red-900/30 text-red-400 px-2 py-0.5 rounded border border-red-500/30 font-mono">API UNAVAILABLE</span>
          </h3>
          <p className="text-white/30 text-sm">Staff schedules and workflow tracking endpoints are currently unavailable in the core API.</p>
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-8 pt-4 border-t border-white/5">
        {/* Manager might have read-only or limited access in a real app, but here they can manage prices/stock */}
        <div className="w-full overflow-hidden">
          <MenuPanel />
        </div>
        <div className="w-full overflow-hidden">
           <InventoryPanel />
        </div>
      </div>
    </div>
  );
}
