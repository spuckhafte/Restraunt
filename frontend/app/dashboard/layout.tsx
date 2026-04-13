"use client";

import React, { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { ApiError, AuthApi } from "@/lib/api";
import {
  clearSession,
  getSession,
  normalizeRole,
  onSessionChange,
  roleToDashboardPath,
  setSession,
  type SessionState,
} from "@/lib/session";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();

  const [session, setSessionState] = useState<SessionState | null>(null);
  const [loading, setLoading] = useState(true);
  const [signingOut, setSigningOut] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const validateSession = async () => {
      const localSession = getSession();
      if (!localSession) {
        router.replace("/login");
        if (isMounted) {
          setSessionState(null);
          setLoading(false);
        }
        return;
      }

      if (isMounted) {
        setSessionState(localSession);
      }

      try {
        const me = await AuthApi.me();
        const effectiveRole = normalizeRole(me.role);
        if (
          me.id !== localSession.userId ||
          me.username !== localSession.username ||
          effectiveRole !== localSession.effectiveRole
        ) {
          setSession({
            token: localSession.token,
            userId: me.id,
            username: me.username,
            baseRole: localSession.baseRole,
            effectiveRole,
          });
        }
      } catch (unknownError) {
        if (unknownError instanceof ApiError && unknownError.status === 401) {
          clearSession();
          router.replace("/login");
        }
      } finally {
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    validateSession();

    const unsubscribe = onSessionChange(() => {
      if (!isMounted) {
        return;
      }
      const nextSession = getSession();
      setSessionState(nextSession);
      if (!nextSession) {
        router.replace("/login");
      }
    });

    return () => {
      isMounted = false;
      unsubscribe();
    };
  }, [router]);

  const workspaceLink = session ? roleToDashboardPath(session.effectiveRole) : "/dashboard";

  const navLinks = useMemo(() => {
    if (!session) {
      return [] as Array<{ href: string; label: string }>;
    }

    const links = [
      { href: "/dashboard", label: "Home" },
      { href: workspaceLink, label: "Workspace" },
    ];

    if (session.baseRole === "MANAGER" && workspaceLink !== "/dashboard/manager") {
      links.push({ href: "/dashboard/manager", label: "Manager" });
    }

    return links;
  }, [session, workspaceLink]);

  const handleLogout = async () => {
    try {
      setSigningOut(true);
      await AuthApi.logout();
    } catch {
      // Force clear local session even if server-side logout fails.
    } finally {
      clearSession();
      router.replace("/login");
      setSigningOut(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen grid place-items-center text-white bg-[radial-gradient(circle_at_20%_20%,rgba(34,197,94,0.15),transparent_35%),linear-gradient(135deg,#050608,#040404)]">
        <p className="text-sm tracking-[0.2em] uppercase text-white/70">Loading workspace...</p>
      </div>
    );
  }

  if (!session) {
    return null;
  }

  return (
    <div className="min-h-screen text-white relative font-sans w-full bg-[radial-gradient(circle_at_10%_10%,rgba(34,197,94,0.16),transparent_35%),radial-gradient(circle_at_95%_5%,rgba(6,182,212,0.14),transparent_30%),linear-gradient(140deg,#06090b,#040404)]">
      <div className="relative z-10 min-h-screen flex flex-col">
        <header className="px-5 md:px-8 py-4 border-b border-white/10 bg-black/30 backdrop-blur-xl sticky top-0 z-50">
          <div className="flex flex-col md:flex-row gap-3 md:items-center md:justify-between max-w-7xl mx-auto">
            <div>
              <h1 className="text-lg sm:text-xl tracking-[0.2em] uppercase text-emerald-300">Restaurant OMS</h1>
              {session && (
                <p className="text-xs text-white/60 mt-1">
                  Signed in as <span className="text-white">{session.username}</span> • Effective role <span className="text-emerald-300">{session.effectiveRole}</span>
                </p>
              )}
            </div>

            <div className="flex flex-wrap items-center gap-2 md:gap-3">
              {navLinks.map((link) => {
                const active = pathname.startsWith(link.href) && (link.href !== "/dashboard" || pathname === "/dashboard");
                return (
                  <Link
                    key={link.href}
                    href={link.href}
                    className={`px-3 py-1.5 rounded-full text-xs uppercase tracking-widest border transition ${
                      active
                        ? "border-emerald-400/70 bg-emerald-500/20 text-emerald-200"
                        : "border-white/15 text-white/70 hover:text-white hover:border-white/40"
                    }`}
                  >
                    {link.label}
                  </Link>
                );
              })}

              <button
                onClick={handleLogout}
                disabled={signingOut}
                className="px-3 py-1.5 rounded-full text-xs uppercase tracking-widest border border-rose-300/40 text-rose-200 hover:bg-rose-500/20 transition disabled:opacity-50"
              >
                {signingOut ? "Signing out..." : "Sign out"}
              </button>
            </div>
          </div>
        </header>

        <main className="flex-1 p-5 md:p-8 max-w-7xl mx-auto w-full">
          {children}
        </main>
      </div>
    </div>
  );
}
