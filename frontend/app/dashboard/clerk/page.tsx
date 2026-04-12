"use client";

import React from "react";
import POSPanel from "@/components/dashboard/POSPanel";

export default function ClerkDashboard() {
  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-3xl font-light text-white tracking-widest uppercase mb-2">Service <span className="font-bold text-cyan-400">Clerk</span></h1>
        <p className="text-white/40 font-mono text-sm">Access Level: Standard // Sector: Point of Sale</p>
      </header>

      <POSPanel />
    </div>
  );
}
