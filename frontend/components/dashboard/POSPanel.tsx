"use client";

import React, { useEffect, useState } from "react";
import { MenuApi, MenuItemDto, SalesApi, SaleEntryRequest, BillDto } from "@/lib/api";

interface CartItem {
  menuItem: MenuItemDto;
  quantity: number;
}

export default function POSPanel() {
  const [menu, setMenu] = useState<MenuItemDto[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lastBill, setLastBill] = useState<BillDto | null>(null);

  const loadMenu = async () => {
    try {
      setLoading(true);
      const data = await MenuApi.getMenu();
      // Only active items for POS
      setMenu(data.filter(item => item.active));
      setError(null);
    } catch (err: any) {
      setError(err.message || "Failed to load menu");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMenu();
  }, []);

  const addToCart = (item: MenuItemDto) => {
    setCart(prev => {
      const existing = prev.find(c => c.menuItem.code === item.code);
      if (existing) {
        return prev.map(c => c.menuItem.code === item.code ? { ...c, quantity: c.quantity + 1 } : c);
      }
      return [...prev, { menuItem: item, quantity: 1 }];
    });
  };

  const removeFromCart = (code: string) => {
    setCart(prev => prev.filter(c => c.menuItem.code !== code));
  };

  const updateQuantity = (code: string, delta: number) => {
    setCart(prev => {
      return prev.map(c => {
        if (c.menuItem.code === code) {
          const newQ = c.quantity + delta;
          return { ...c, quantity: Math.max(1, newQ) };
        }
        return c;
      });
    });
  };

  const cartTotal = cart.reduce((sum, item) => sum + (item.menuItem.basePrice * item.quantity), 0);

  const handleCheckout = async () => {
    if (cart.length === 0) return;
    try {
      setProcessing(true);
      const entries: SaleEntryRequest[] = cart.map(c => ({
        itemCode: c.menuItem.code,
        quantity: c.quantity
      }));
      
      const bill = await SalesApi.processSale({ entries });
      setLastBill(bill);
      setCart([]);
      setError(null);
    } catch (err: any) {
      setError(err.message || "Failed to process sale");
    } finally {
      setProcessing(false);
    }
  };

  const handleVoid = async (billId: number) => {
    try {
      setProcessing(true);
      await SalesApi.voidSale(billId);
      if (lastBill?.id === billId) {
        setLastBill({ ...lastBill, voided: true });
      }
      alert(`Bill #${billId} successfully voided.`);
    } catch (err: any) {
      setError(err.message || "Failed to void bill");
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
      {/* Menu Selection Area */}
      <div className="lg:col-span-2 bg-black/80 border border-white/10 rounded-2xl p-6 backdrop-blur-md mix-blend-color-dodge flex flex-col h-[700px]">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-light text-cyan-400 tracking-widest uppercase flex items-center gap-3">
            Point of Sale
            {loading && <div className="w-4 h-4 rounded-full border-2 border-cyan-500 border-t-transparent animate-spin" />}
          </h2>
          <button onClick={loadMenu} className="text-xs text-white/50 hover:text-cyan-400 uppercase tracking-widest">
            Reload Menu
          </button>
        </div>

        {error && (
          <div className="mb-4 p-3 bg-red-900/20 border border-red-500/50 text-red-400 rounded-lg text-sm">
            {error}
          </div>
        )}

        <div className="flex-1 overflow-y-auto grid grid-cols-2 sm:grid-cols-3 gap-4 pb-4 pr-2 custom-scrollbar">
          {menu.map(item => (
            <button
              key={item.code}
              onClick={() => addToCart(item)}
              className="bg-black/40 border border-white/5 hover:border-cyan-500/50 rounded-xl p-4 flex flex-col items-start justify-between text-left transition-all hover:bg-white/[0.05] group h-32"
            >
              <div>
                <div className="text-cyan-400 font-mono text-xs mb-1">{item.code}</div>
                <div className="text-white font-medium text-sm line-clamp-2 leading-tight group-hover:text-cyan-100">{item.name}</div>
              </div>
              <div className="text-emerald-400 font-mono text-lg mt-2">${item.basePrice.toFixed(2)}</div>
            </button>
          ))}
          {menu.length === 0 && !loading && (
            <div className="col-span-full text-center text-white/30 uppercase tracking-widest mt-10">
              No active menu items
            </div>
          )}
        </div>
      </div>

      {/* Cart & Checkout Area */}
      <div className="bg-black/80 border border-white/10 rounded-2xl p-6 backdrop-blur-md mix-blend-color-dodge flex flex-col h-[700px]">
        <h2 className="text-xl font-light text-white tracking-widest uppercase mb-6 border-b border-white/10 pb-4">
          Current Order
        </h2>

        <div className="flex-1 overflow-y-auto space-y-4 pr-2 custom-scrollbar">
          {cart.map(c => (
            <div key={c.menuItem.code} className="bg-black/20 border border-white/5 rounded-lg p-3">
              <div className="flex justify-between items-start mb-2">
                <span className="text-sm font-medium text-white/90">{c.menuItem.name}</span>
                <button onClick={() => removeFromCart(c.menuItem.code)} className="text-white/30 hover:text-red-400 text-xs">
                  ✕
                </button>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-3 bg-black flex-1 rounded border border-white/5 p-1 max-w-[120px]">
                  <button onClick={() => updateQuantity(c.menuItem.code, -1)} className="w-6 h-6 flex items-center justify-center text-white/50 hover:text-cyan-400 hover:bg-white/5 rounded">-</button>
                  <span className="text-sm font-mono flex-1 text-center">{c.quantity}</span>
                  <button onClick={() => updateQuantity(c.menuItem.code, 1)} className="w-6 h-6 flex items-center justify-center text-white/50 hover:text-cyan-400 hover:bg-white/5 rounded">+</button>
                </div>
                <div className="text-emerald-400 font-mono text-sm">
                  ${(c.menuItem.basePrice * c.quantity).toFixed(2)}
                </div>
              </div>
            </div>
          ))}
          {cart.length === 0 && (
            <div className="text-center text-white/30 uppercase tracking-widest text-sm mt-10">
              Cart is empty
            </div>
          )}
        </div>

        <div className="pt-4 border-t border-white/10 mt-4">
          <div className="flex justify-between items-center mb-6">
            <span className="text-white/50 uppercase tracking-wider text-sm">Total</span>
            <span className="text-emerald-400 font-mono text-3xl font-light">${cartTotal.toFixed(2)}</span>
          </div>
          
          <button
            onClick={handleCheckout}
            disabled={cart.length === 0 || processing}
            className="w-full bg-cyan-600 hover:bg-cyan-500 disabled:bg-white/5 disabled:text-white/30 text-white py-4 rounded-xl font-bold tracking-widest uppercase transition-all shadow-[0_0_15px_rgba(0,255,255,0.2)] disabled:shadow-none relative overflow-hidden"
          >
            {processing ? (
              <span className="animate-pulse">Processing...</span>
            ) : (
              "Checkout"
            )}
          </button>
        </div>

        {/* Quick Last Receipt View */}
        {lastBill && (
          <div className="mt-4 p-4 bg-emerald-900/10 border border-emerald-500/20 rounded-xl relative overflow-hidden group">
            <div className="absolute top-0 left-0 w-1 h-full bg-emerald-500"></div>
            <div className="flex justify-between items-center mb-1">
              <span className="text-xs text-emerald-400/80 uppercase tracking-wider">Last Bill #{lastBill.id}</span>
              {lastBill.voided && <span className="bg-red-500/20 text-red-400 text-[10px] px-2 py-0.5 rounded font-mono">VOIDED</span>}
            </div>
            <div className="text-white font-mono text-sm">${lastBill.subtotal.toFixed(2)}</div>
            {!lastBill.voided && (
              <button 
                onClick={() => handleVoid(lastBill.id)}
                disabled={processing}
                className="mt-2 text-[10px] uppercase tracking-wider text-white/40 hover:text-red-400 transition-colors"
               >
                Void Transaction
               </button>
            )}
          </div>
        )}
      </div>

    </div>
  );
}
