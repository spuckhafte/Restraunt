export type AppRole = "MANAGER" | "SALES" | "INVENTORY";

export interface SessionState {
  token: string;
  userId: number;
  username: string;
  baseRole: AppRole;
  effectiveRole: AppRole;
}

const SESSION_KEY = "restaurant.session";
const SESSION_CHANGED_EVENT = "restaurant-session-changed";

function isBrowser(): boolean {
  return typeof window !== "undefined";
}

export function normalizeRole(rawRole: string): AppRole {
  const role = rawRole.trim().toUpperCase();
  if (role === "MANAGER" || role === "SALES" || role === "INVENTORY") {
    return role;
  }
  throw new Error(`Unsupported role: ${rawRole}`);
}

export function roleToDashboardPath(role: AppRole): string {
  if (role === "MANAGER") {
    return "/dashboard/manager";
  }
  if (role === "SALES") {
    return "/dashboard/clerk";
  }
  return "/dashboard/inventory";
}

function emitSessionChange(): void {
  if (!isBrowser()) {
    return;
  }
  window.dispatchEvent(new Event(SESSION_CHANGED_EVENT));
}

export function onSessionChange(listener: () => void): () => void {
  if (!isBrowser()) {
    return () => undefined;
  }
  const wrappedListener = () => listener();
  window.addEventListener(SESSION_CHANGED_EVENT, wrappedListener);
  window.addEventListener("storage", wrappedListener);
  return () => {
    window.removeEventListener(SESSION_CHANGED_EVENT, wrappedListener);
    window.removeEventListener("storage", wrappedListener);
  };
}

export function getSession(): SessionState | null {
  if (!isBrowser()) {
    return null;
  }

  const raw = window.localStorage.getItem(SESSION_KEY);
  if (!raw) {
    return null;
  }

  try {
    const parsed = JSON.parse(raw) as {
      token: string;
      userId: number;
      username: string;
      baseRole: string;
      effectiveRole: string;
    };

    if (!parsed.token || !parsed.username) {
      return null;
    }

    return {
      token: parsed.token,
      userId: Number(parsed.userId),
      username: parsed.username,
      baseRole: normalizeRole(parsed.baseRole),
      effectiveRole: normalizeRole(parsed.effectiveRole),
    };
  } catch {
    return null;
  }
}

export function setSession(session: {
  token: string;
  userId: number;
  username: string;
  baseRole: string;
  effectiveRole?: string;
}): void {
  if (!isBrowser()) {
    return;
  }

  const normalized: SessionState = {
    token: session.token,
    userId: Number(session.userId),
    username: session.username,
    baseRole: normalizeRole(session.baseRole),
    effectiveRole: normalizeRole(session.effectiveRole ?? session.baseRole),
  };

  window.localStorage.setItem(SESSION_KEY, JSON.stringify(normalized));
  emitSessionChange();
}

export function updateEffectiveRole(effectiveRole: string): void {
  const current = getSession();
  if (!current) {
    return;
  }
  setSession({
    token: current.token,
    userId: current.userId,
    username: current.username,
    baseRole: current.baseRole,
    effectiveRole,
  });
}

export function clearSession(): void {
  if (!isBrowser()) {
    return;
  }
  window.localStorage.removeItem(SESSION_KEY);
  emitSessionChange();
}
