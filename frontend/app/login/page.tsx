"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError, AuthApi } from "@/lib/api";
import { getSession, normalizeRole, roleToDashboardPath, setSession } from "@/lib/session";

export default function LoginPage() {
  const router = useRouter();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const session = getSession();
    if (session) {
      router.replace(roleToDashboardPath(session.effectiveRole));
    }
  }, [router]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!username.trim() || !password.trim()) {
      setError("Enter both username and password.");
      return;
    }

    try {
      setSubmitting(true);
      setError(null);

      const response = await AuthApi.login({
        username: username.trim(),
        password,
      });

      setSession({
        token: response.sessionToken,
        userId: response.user.id,
        username: response.user.username,
        baseRole: response.user.role,
        effectiveRole: response.user.role,
      });

      router.replace(roleToDashboardPath(normalizeRole(response.user.role)));
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Unable to sign in right now.");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="min-h-screen text-white bg-[radial-gradient(circle_at_15%_20%,rgba(34,197,94,0.16),transparent_40%),radial-gradient(circle_at_85%_10%,rgba(6,182,212,0.2),transparent_35%),linear-gradient(135deg,#070b0a,#040505)] flex items-center justify-center px-4 py-12">
      <section className="w-full max-w-xl border border-emerald-400/20 bg-black/40 backdrop-blur-xl rounded-3xl p-8 sm:p-10 shadow-[0_20px_60px_rgba(0,0,0,0.45)]">
        <p className="text-xs tracking-[0.25em] uppercase text-emerald-300/80 mb-3">Restaurant OMS</p>
        <h1 className="text-3xl sm:text-4xl tracking-tight mb-2">Staff Login</h1>
        <p className="text-white/60 text-sm mb-8">Authenticate with your backend-seeded account to access the assigned dashboard.</p>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label htmlFor="username" className="block text-sm text-white/70 mb-2">Username</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              placeholder="manager"
              className="w-full rounded-xl border border-white/15 bg-white/[0.03] px-4 py-3 outline-none focus:border-emerald-400/70 transition"
              autoComplete="username"
            />
          </div>

          <div>
            <label htmlFor="password" className="block text-sm text-white/70 mb-2">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="••••••••"
              className="w-full rounded-xl border border-white/15 bg-white/[0.03] px-4 py-3 outline-none focus:border-emerald-400/70 transition"
              autoComplete="current-password"
            />
          </div>

          {error && (
            <p className="text-sm text-red-300 bg-red-900/20 border border-red-400/30 rounded-xl px-4 py-3">{error}</p>
          )}

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-xl bg-emerald-500 text-black font-semibold py-3.5 hover:bg-emerald-400 transition disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {submitting ? "Signing in..." : "Sign In"}
          </button>
        </form>

        <div className="mt-8 rounded-xl border border-white/10 bg-white/[0.03] px-4 py-4 text-xs text-white/70 leading-6">
          <p className="uppercase tracking-wider text-white/55 mb-2">Seeded users</p>
          <p>manager / manager123</p>
          <p>sales / sales123</p>
          <p>inventory / inventory123</p>
        </div>
      </section>
    </main>
  );
}
