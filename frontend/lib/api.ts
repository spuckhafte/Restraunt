// frontend/lib/api.ts

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api';

/** ==============================
 *  MODELS
 *  ============================== */

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
  quantity: number;
  pricePerUnit: number;
  total: number;
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

// Request Models
export interface CreateInventoryItemRequest {
  code: string;
  name: string;
  unit: string;
  quantityOnHand: number;
}

export interface QuantityRequest {
  quantity: number;
}

export interface CreateMenuItemRequest {
  code: string;
  name: string;
  basePrice: number;
}

export interface UpdatePriceRequest {
  newPrice: number;
}

export interface SaleEntryRequest {
  itemCode: string;
  quantity: number;
}

export interface CreateSaleRequest {
  entries: SaleEntryRequest[];
}

/** ==============================
 *  API FETCH HELPERS
 *  ============================== */

async function fetchApi<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers || {}),
    },
  });

  if (!response.ok) {
    const errorData = await response.text().catch(() => null);
    throw new Error(`API Request Failed: ${response.status} ${response.statusText} ${errorData ? '- ' + errorData : ''}`);
  }

  // Some endpoints might return empty body (e.g. DELETE)
  if (response.status === 204) {
    return {} as T;
  }

  try {
    return await response.json();
  } catch (err) {
    return {} as T;
  }
}

/** ==============================
 *  INVENTORY API
 *  ============================== */
export const InventoryApi = {
  list: () => fetchApi<InventoryItemDto[]>('/inventory'),
  
  add: (data: CreateInventoryItemRequest) => fetchApi<InventoryItemDto>('/inventory', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  receive: (code: string, quantity: number) => fetchApi<InventoryItemDto>(`/inventory/${encodeURIComponent(code)}/receive`, {
    method: 'POST',
    body: JSON.stringify({ quantity }),
  }),

  issue: (code: string, quantity: number) => fetchApi<IssueResultDto>(`/inventory/${encodeURIComponent(code)}/issue`, {
    method: 'POST',
    body: JSON.stringify({ quantity }),
  }),
};

/** ==============================
 *  MENU API
 *  ============================== */
export const MenuApi = {
  getMenu: () => fetchApi<MenuItemDto[]>('/menu'),
  
  addItem: (data: CreateMenuItemRequest) => fetchApi<MenuItemDto>('/menu', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  updatePrice: (code: string, newPrice: number) => fetchApi<MenuItemDto>(`/menu/${encodeURIComponent(code)}/price`, {
    method: 'PUT',
    body: JSON.stringify({ newPrice }),
  }),

  deleteItem: (code: string) => fetchApi<{ message?: string }>(`/menu/${encodeURIComponent(code)}`, {
    method: 'DELETE',
  }),
};

/** ==============================
 *  SALES API
 *  ============================== */
export const SalesApi = {
  processSale: (data: CreateSaleRequest) => fetchApi<BillDto>('/sales', {
    method: 'POST',
    body: JSON.stringify(data),
  }),

  voidSale: (billId: number) => fetchApi<BillDto>(`/sales/${encodeURIComponent(billId)}/void`, {
    method: 'POST',
  }),
};
