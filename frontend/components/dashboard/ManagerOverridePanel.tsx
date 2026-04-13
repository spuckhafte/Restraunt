"use client";

import { useState } from "react";
import { ApiError, AuthApi, ManagerOverrideApi } from "@/lib/api";
import { getSession, updateEffectiveRole } from "@/lib/session";

export default function ManagerOverridePanel() {
  const [targetRole, setTargetRole] = useState<"sales" | "inventory">("sales");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const session = getSession();
  const canUseOverride = session?.baseRole === "MANAGER";

  const syncEffectiveRole = async () => {
    const me = await AuthApi.me();
    updateEffectiveRole(me.role);
  };

  const assumeRole = async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await ManagerOverrideApi.assumeRole(targetRole);
      await syncEffectiveRole();
      setMessage(result.message);
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Unable to switch role right now.");
      }
      setMessage(null);
    } finally {
      setLoading(false);
    }
  };

  const restoreRole = async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await ManagerOverrideApi.restoreRole();
      await syncEffectiveRole();
      setMessage(result.message);
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Unable to restore role right now.");
      }
      setMessage(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="rounded-2xl border border-white/10 bg-black/20 p-5 sm:p-6 space-y-4">
      <div>
        <h3 className="text-2xl text-amber-200">Manager Role Override</h3>
        <p className="text-sm text-white/60">UC-05: Assume SALES or INVENTORY role for operational continuity and restore later.</p>
      </div>

      {!canUseOverride && (
        <p className="rounded-lg border border-amber-400/30 bg-amber-900/20 px-4 py-3 text-sm text-amber-200">
          This control is available only to the base MANAGER account.
        </p>
      )}

      <div className="flex flex-wrap items-center gap-3">
        <select
          value={targetRole}
          onChange={(event) => setTargetRole(event.target.value as "sales" | "inventory")}
          disabled={!canUseOverride || loading}
          className="rounded-lg border border-white/15 bg-white/[0.04] px-3 py-2 text-sm disabled:opacity-50"
        >
          <option value="sales">SALES</option>
          <option value="inventory">INVENTORY</option>
        </select>

        <button
          type="button"
          onClick={assumeRole}
          disabled={!canUseOverride || loading}
          className="rounded-lg border border-amber-300/60 bg-amber-500/20 text-amber-100 px-4 py-2 text-sm hover:bg-amber-500/30 transition disabled:opacity-50"
        >
          Assume Role
        </button>

        <button
          type="button"
          onClick={restoreRole}
          disabled={!canUseOverride || loading}
          className="rounded-lg border border-emerald-300/60 bg-emerald-500/20 text-emerald-100 px-4 py-2 text-sm hover:bg-emerald-500/30 transition disabled:opacity-50"
        >
          Restore MANAGER
        </button>
      </div>

      {error && <p className="rounded-lg border border-red-400/30 bg-red-900/20 px-4 py-3 text-sm text-red-200">{error}</p>}
      {message && <p className="rounded-lg border border-emerald-400/30 bg-emerald-900/20 px-4 py-3 text-sm text-emerald-100">{message}</p>}
    </section>
  );
}
