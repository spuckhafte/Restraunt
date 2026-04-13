import { clearSession, getSession } from "@/lib/session";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "/api";

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export interface SimpleMessage {
  message: string;
}

export interface AuthUserDto {
  id: number;
  username: string;
  role: string;
}

export interface LoginResponse {
  sessionToken: string;
  user: AuthUserDto;
}

export interface InventoryItemDto {
  code: string;
  name: string;
  unit: string;
  quantityOnHand: number;
  reorderThreshold: number;
}

export interface MenuItemDto {
  code: string;
  name: string;
  basePrice: number;
  active: boolean;
}

export interface SaleLineDto {
  itemCode: string;
  itemName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface BillDto {
  id: number;
  lines: SaleLineDto[];
  subtotal: number;
  createdAt: string;
  voided: boolean;
}

export interface IssueResultDto {
  itemCode: string;
  quantityIssued: number;
  remainingQuantity: number;
}

export interface SupplierInvoiceDto {
  id: number;
  supplierName: string;
  itemCode: string;
  quantity: number;
  unitPrice: number;
  totalAmount: number;
  invoiceDate: string;
  approved: boolean;
  paid: boolean;
  flaggedForReview: boolean;
}

export interface SalesSummaryDto {
  month: string;
  billsCount: number;
  totalSales: number;
}

export interface ItemPerformanceDto {
  itemCode: string;
  itemName: string;
  quantitySold: number;
  revenue: number;
}

export interface ExpenseSummaryDto {
  invoiceCount: number;
  totalExpenses: number;
  paidExpenses: number;
  unpaidExpenses: number;
}

export interface InventoryTrendDto {
  itemCode: string;
  itemName: string;
  issuedToday: number;
  threeDayAverage: number;
  flaggedUnusual: boolean;
}

export interface LiquidityStatusDto {
  cashBalance: number;
  checksIssued: number;
  totalCheckPayments: number;
}

export interface DashboardReportDto {
  salesSummaries: SalesSummaryDto[];
  itemPerformance: ItemPerformanceDto[];
  expenseSummary: ExpenseSummaryDto;
  inventoryTrends: InventoryTrendDto[];
  liquidity: LiquidityStatusDto;
}

export interface CheckPaymentDto {
  invoiceId: number;
  checkNumber: string;
  amount: number;
  generatedAt: string;
  pdfBase64: string;
  cashBalanceAfterPayment: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface CreateInventoryItemRequest {
  code: string;
  name: string;
  unit: string;
  quantityOnHand: number;
}

export interface CreateMenuItemRequest {
  code: string;
  name: string;
  basePrice: number;
}

export interface SaleEntryRequest {
  itemCode: string;
  quantity: number;
}

export interface CreateSaleRequest {
  entries: SaleEntryRequest[];
}

export interface CreateSupplierInvoiceRequest {
  supplierName: string;
  itemCode: string;
  quantity: number;
  unitPrice: number;
  invoiceDate: string;
  approved: boolean;
}

interface FetchApiOptions extends RequestInit {
  auth?: boolean;
}

function parseBackendMessage(payload: unknown): string | null {
  if (!payload || typeof payload !== "object") {
    return null;
  }

  const maybePayload = payload as { message?: unknown; error?: unknown };
  if (typeof maybePayload.message === "string" && maybePayload.message.trim()) {
    return maybePayload.message;
  }
  if (typeof maybePayload.error === "string" && maybePayload.error.trim()) {
    return maybePayload.error;
  }
  return null;
}

async function fetchApi<T>(endpoint: string, options?: FetchApiOptions): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  const shouldAttachAuth = options?.auth !== false;
  const headers = new Headers(options?.headers);

  if (!headers.has("Content-Type") && options?.body) {
    headers.set("Content-Type", "application/json");
  }

  if (shouldAttachAuth) {
    const session = getSession();
    if (session?.token) {
      headers.set("X-Session-Token", session.token);
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 204) {
    return {} as T;
  }

  const contentType = response.headers.get("content-type") || "";
  let parsedPayload: unknown = null;

  if (contentType.includes("application/json")) {
    parsedPayload = await response.json().catch(() => null);
  } else {
    const rawText = await response.text().catch(() => "");
    parsedPayload = rawText || null;
  }

  if (!response.ok) {
    if (response.status === 401) {
      clearSession();
    }

    const backendMessage =
      typeof parsedPayload === "string"
        ? parsedPayload.trim()
        : parseBackendMessage(parsedPayload);
    const message = backendMessage || `${response.status} ${response.statusText}`;
    throw new ApiError(message, response.status);
  }

  if (parsedPayload === null || parsedPayload === "") {
    return {} as T;
  }

  return parsedPayload as T;
}

export const AuthApi = {
  login: (data: LoginRequest) =>
    fetchApi<LoginResponse>("/auth/login", {
      method: "POST",
      auth: false,
      body: JSON.stringify(data),
    }),

  logout: () =>
    fetchApi<SimpleMessage>("/auth/logout", {
      method: "POST",
    }),

  me: () => fetchApi<AuthUserDto>("/auth/me"),
};

export const InventoryApi = {
  list: () => fetchApi<InventoryItemDto[]>("/inventory"),

  add: (data: CreateInventoryItemRequest) =>
    fetchApi<InventoryItemDto>("/inventory", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  receive: (code: string, quantity: number) =>
    fetchApi<InventoryItemDto>(`/inventory/${encodeURIComponent(code)}/receive`, {
      method: "POST",
      body: JSON.stringify({ quantity }),
    }),

  issue: (code: string, quantity: number) =>
    fetchApi<IssueResultDto>(`/inventory/${encodeURIComponent(code)}/issue`, {
      method: "POST",
      body: JSON.stringify({ quantity }),
    }),

  listInvoices: () => fetchApi<SupplierInvoiceDto[]>("/inventory/invoices"),

  createInvoice: (data: CreateSupplierInvoiceRequest) =>
    fetchApi<SupplierInvoiceDto>("/inventory/invoices", {
      method: "POST",
      body: JSON.stringify(data),
    }),
};

export const MenuApi = {
  getMenu: () => fetchApi<MenuItemDto[]>("/menu"),

  addItem: (data: CreateMenuItemRequest) =>
    fetchApi<MenuItemDto>("/menu", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  updatePrice: (code: string, newPrice: number) =>
    fetchApi<MenuItemDto>(`/menu/${encodeURIComponent(code)}/price`, {
      method: "PUT",
      body: JSON.stringify({ newPrice }),
    }),

  deleteItem: (code: string) =>
    fetchApi<SimpleMessage>(`/menu/${encodeURIComponent(code)}`, {
      method: "DELETE",
    }),
};

export const SalesApi = {
  processSale: (data: CreateSaleRequest) =>
    fetchApi<BillDto>("/sales", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  voidSale: (billId: number) =>
    fetchApi<BillDto>(`/sales/${encodeURIComponent(billId)}/void`, {
      method: "POST",
    }),
};

export const ReportsApi = {
  dashboard: (from?: string, to?: string) => {
    const params = new URLSearchParams();
    if (from) {
      params.set("from", from);
    }
    if (to) {
      params.set("to", to);
    }
    const query = params.toString();
    return fetchApi<DashboardReportDto>(`/reports/dashboard${query ? `?${query}` : ""}`);
  },
};

export const ManagerOverrideApi = {
  assumeRole: (role: "sales" | "inventory" | "manager") =>
    fetchApi<SimpleMessage>(`/manager/override/assume/${encodeURIComponent(role)}`, {
      method: "POST",
    }),

  restoreRole: () =>
    fetchApi<SimpleMessage>("/manager/override/restore", {
      method: "POST",
    }),
};

export const PaymentsApi = {
  generateCheck: (invoiceId: number) =>
    fetchApi<CheckPaymentDto>("/payments/checks/generate", {
      method: "POST",
      body: JSON.stringify({ invoiceId }),
    }),
};
