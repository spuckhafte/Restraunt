"use client";

import React from "react";
import MenuPanel from "@/components/dashboard/MenuPanel";
import InventoryPanel from "@/components/dashboard/InventoryPanel";

export default function AdminDashboard() {
  return (
    <div className="space-y-8">
      <header>
        <h1 className="text-3xl font-light text-white tracking-widest uppercase mb-2">System <span className="font-bold text-cyan-400">Administrator</span></h1>
        <p className="text-white/40 font-mono text-sm">Access Level: Maximum // Sector: Global Command</p>
      </header>

      {/* Placeholders for unimplemented APIs */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-black/80 backdrop-blur-md border border-white/5 rounded-2xl p-6 relative overflow-hidden group">
          <div className="absolute inset-0 bg-gradient-to-r from-red-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
          <h3 className="text-white/70 tracking-widest uppercase mb-2 flex justify-between items-center">
            Global Analytics
            <span className="text-[10px] bg-red-900/30 text-red-400 px-2 py-0.5 rounded border border-red-500/30 font-mono">API UNAVAILABLE</span>
          </h3>
          <p className="text-white/30 text-sm">System metric collection endpoints are offline or not yet deployed to the server infrastructure.</p>
        </div>
        <div className="bg-black/80 backdrop-blur-md border border-white/5 rounded-2xl p-6 relative overflow-hidden group">
          <div className="absolute inset-0 bg-gradient-to-r from-red-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"></div>
          <h3 className="text-white/70 tracking-widest uppercase mb-2 flex justify-between items-center">
            User Operations Control
            <span className="text-[10px] bg-red-900/30 text-red-400 px-2 py-0.5 rounded border border-red-500/30 font-mono">API UNAVAILABLE</span>
          </h3>
          <p className="text-white/30 text-sm">Staff access control endpoints are offline. Currently running in decentralized auth mode.</p>
        </div>
      </div>

      <div className="pt-4 border-t border-white/5">
        <MenuPanel />
      </div>

      <div className="pt-4 border-t border-white/5">
        <InventoryPanel />
      </div>
    </div>
  );
}
