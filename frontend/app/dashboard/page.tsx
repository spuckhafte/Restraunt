"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { getSession, onSessionChange, roleToDashboardPath, type SessionState } from "@/lib/session";

export default function DashboardHomePage() {
  const [session, setSession] = useState<SessionState | null>(() => getSession());

  useEffect(() => {
    const unsubscribe = onSessionChange(() => {
      setSession(getSession());
    });
    return unsubscribe;
  }, []);

  const destination = useMemo(() => {
    if (!session) {
      return "/login";
    }
    return roleToDashboardPath(session.effectiveRole);
  }, [session]);

  return (
    <section className="space-y-6">
      <header className="space-y-2">
        <p className="text-xs uppercase tracking-[0.25em] text-emerald-200/70">Operations Dashboard</p>
        <h2 className="text-3xl sm:text-4xl font-semibold">Welcome back{session ? `, ${session.username}` : ""}.</h2>
        <p className="text-white/70 max-w-2xl">
          This workspace is now connected to the Spring Boot backend with token-based authentication, role policies,
          invoices, reports, sales billing, inventory controls, and supplier check generation.
        </p>
      </header>

      <div className="grid md:grid-cols-2 gap-4">
        <article className="rounded-2xl border border-white/15 bg-white/[0.03] p-5">
          <p className="text-xs uppercase tracking-widest text-white/60 mb-2">Current Access</p>
          <p className="text-2xl font-semibold text-emerald-200">{session?.effectiveRole ?? "GUEST"}</p>
          <p className="text-sm text-white/60 mt-2">
            Base role: <span className="text-white">{session?.baseRole ?? "Unknown"}</span>
          </p>
        </article>

        <article className="rounded-2xl border border-white/15 bg-white/[0.03] p-5">
          <p className="text-xs uppercase tracking-widest text-white/60 mb-2">Next Step</p>
          <p className="text-white/85 mb-4">Open your operational workspace and continue from the correct use-case panel.</p>
          <Link
            href={destination}
            className="inline-flex rounded-xl bg-emerald-500 text-black px-4 py-2.5 text-sm font-semibold hover:bg-emerald-400 transition"
          >
            Open Workspace
          </Link>
        </article>
      </div>
    </section>
  );
}
