"use client";

import React from "react";
import POSPanel from "@/components/dashboard/POSPanel";

export default function ClerkDashboard() {
  return (
    <div className="space-y-6">
      <header>
        <h1 className="text-3xl font-light text-white tracking-widest uppercase mb-2">Sales <span className="font-bold text-cyan-300">Workspace</span></h1>
        <p className="text-white/50 text-sm">UC-02 point-of-sale billing and void management.</p>
      </header>

      <POSPanel />
    </div>
  );
}
