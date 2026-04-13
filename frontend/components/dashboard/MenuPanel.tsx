"use client";

import React, { useEffect, useState } from "react";
import { ApiError, MenuApi, MenuItemDto } from "@/lib/api";

export default function MenuPanel({ readOnly = false }: { readOnly?: boolean }) {
  const [items, setItems] = useState<MenuItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Form states
  const [code, setCode] = useState("");
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");

  const loadMenu = async () => {
    try {
      setLoading(true);
      const data = await MenuApi.getMenu();
      setItems(data);
      setError(null);
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to load menu");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMenu();
  }, []);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (readOnly) return;
    try {
      await MenuApi.addItem({ code, name, basePrice: parseFloat(price) });
      setCode("");
      setName("");
      setPrice("");
      loadMenu();
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to add item");
      }
    }
  };

  const handleDelete = async (code: string) => {
    if (readOnly) return;
    try {
      await MenuApi.deleteItem(code);
      loadMenu();
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to delete item");
      }
    }
  };

  const handleUpdatePrice = async (code: string, currentPrice: number) => {
    if (readOnly) return;
    const newPriceStr = prompt("Enter new price:", currentPrice.toString());
    if (!newPriceStr) return;
    const newPrice = parseFloat(newPriceStr);
    if (isNaN(newPrice)) return;

    try {
      await MenuApi.updatePrice(code, newPrice);
      loadMenu();
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to update price");
      }
    }
  };

  return (
    <div className="bg-black/80 border border-white/10 rounded-2xl p-6 backdrop-blur-md mix-blend-color-dodge">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-light text-cyan-400 tracking-widest uppercase flex items-center gap-3">
          Menu Database
          {loading && <div className="w-4 h-4 rounded-full border-2 border-cyan-500 border-t-transparent animate-spin" />}
        </h2>
        <button onClick={loadMenu} className="text-xs text-white/50 hover:text-cyan-400 uppercase tracking-widest transition-colors">
          Refresh
        </button>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-900/20 border border-red-500/50 text-red-400 rounded-lg text-sm">
          {error}
        </div>
      )}

      {!readOnly && (
        <form onSubmit={handleAdd} className="mb-8 grid grid-cols-1 sm:grid-cols-4 gap-4 bg-black/20 p-4 rounded-xl border border-white/5">
          <input
            type="text"
            placeholder="Item Code (e.g. B1)"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            required
            className="bg-white/5 border border-white/10 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-cyan-500/50 transition-colors"
          />
          <input
            type="text"
            placeholder="Item Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            className="bg-white/5 border border-white/10 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-cyan-500/50 transition-colors sm:col-span-2"
          />
          <div className="flex gap-2">
            <input
              type="number"
              step="0.01"
              placeholder="Price"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
              className="bg-white/5 border border-white/10 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-cyan-500/50 transition-colors w-full"
            />
            <button type="submit" className="bg-cyan-500/20 text-cyan-300 border border-cyan-500/50 rounded-lg px-4 hover:bg-cyan-500/30 transition-colors font-bold">
              +
            </button>
          </div>
        </form>
      )}

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="text-white/40 uppercase tracking-widest border-b border-white/10">
              <th className="pb-3 font-medium">Code</th>
              <th className="pb-3 font-medium">Name</th>
              <th className="pb-3 font-medium">Price</th>
              <th className="pb-3 font-medium">Status</th>
              {!readOnly && <th className="pb-3 font-medium text-right">Actions</th>}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.code} className="border-b border-white/5 hover:bg-white/[0.02] transition-colors group">
                <td className="py-3 text-cyan-400 font-mono">{item.code}</td>
                <td className="py-3 text-white/90">{item.name}</td>
                <td className="py-3 text-emerald-400 font-mono">${item.basePrice.toFixed(2)}</td>
                <td className="py-3">
                  <span className={`px-2 py-1 rounded text-xs tracking-wider ${item.active ? 'bg-emerald-900/30 text-emerald-400' : 'bg-red-900/30 text-red-400'}`}>
                    {item.active ? 'ACTIVE' : 'INACTIVE'}
                  </span>
                </td>
                {!readOnly && (
                  <td className="py-3 text-right opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => handleUpdatePrice(item.code, item.basePrice)}
                      className="text-cyan-400 hover:text-cyan-300 text-xs uppercase tracking-wider mr-4"
                    >
                      Update Price
                    </button>
                    <button
                      onClick={() => handleDelete(item.code)}
                      className="text-fuchsia-400 hover:text-fuchsia-300 text-xs uppercase tracking-wider"
                    >
                      Delete
                    </button>
                  </td>
                )}
              </tr>
            ))}
            {items.length === 0 && !loading && (
              <tr>
                <td colSpan={5} className="py-8 text-center text-white/30 tracking-widest uppercase">
                  No menu items found
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
