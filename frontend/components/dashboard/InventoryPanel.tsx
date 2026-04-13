"use client";

import React, { useEffect, useState } from "react";
import { ApiError, InventoryApi, InventoryItemDto } from "@/lib/api";

export default function InventoryPanel({ readOnly = false }: { readOnly?: boolean }) {
  const [items, setItems] = useState<InventoryItemDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Add Item form states
  const [code, setCode] = useState("");
  const [name, setName] = useState("");
  const [unit, setUnit] = useState("");
  const [quantity, setQuantity] = useState("");

  const loadInventory = async () => {
    try {
      setLoading(true);
      const data = await InventoryApi.list();
      setItems(data);
      setError(null);
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to load inventory");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInventory();
  }, []);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (readOnly) return;
    try {
      await InventoryApi.add({
        code,
        name,
        unit,
        quantityOnHand: parseFloat(quantity)
      });
      setCode("");
      setName("");
      setUnit("");
      setQuantity("");
      loadInventory();
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError("Failed to add inventory item");
      }
    }
  };

  const handleAction = async (itemCode: string, actionName: "receive" | "issue") => {
    if (readOnly) return;
    const qtyStr = prompt(`Enter quantity to ${actionName.toUpperCase()} for ${itemCode}:`, "1");
    if (!qtyStr) return;
    const qty = parseFloat(qtyStr);
    if (isNaN(qty) || qty <= 0) return;

    try {
      if (actionName === "receive") {
        await InventoryApi.receive(itemCode, qty);
      } else {
        await InventoryApi.issue(itemCode, qty);
      }
      loadInventory();
    } catch (unknownError) {
      if (unknownError instanceof ApiError) {
        setError(unknownError.message);
      } else {
        setError(`Failed to ${actionName} item`);
      }
    }
  };

  return (
    <div className="bg-black/80 border border-white/10 rounded-2xl p-6 backdrop-blur-md mix-blend-color-dodge">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-light text-fuchsia-400 tracking-widest uppercase flex items-center gap-3">
          Inventory Control
          {loading && <div className="w-4 h-4 rounded-full border-2 border-fuchsia-500 border-t-transparent animate-spin" />}
        </h2>
        <button onClick={loadInventory} className="text-xs text-white/50 hover:text-fuchsia-400 uppercase tracking-widest transition-colors">
          Refresh
        </button>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-900/20 border border-red-500/50 text-red-400 rounded-lg text-sm">
          {error}
        </div>
      )}

      {!readOnly && (
        <form onSubmit={handleAdd} className="mb-8 grid grid-cols-2 md:grid-cols-5 gap-4 bg-black/20 p-4 rounded-xl border border-white/5">
          <input
            type="text"
            placeholder="Code"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            required
            className="bg-white/5 border border-white/10 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-fuchsia-500/50 transition-colors"
          />
          <input
            type="text"
            placeholder="Item Name"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            className="bg-white/5 border border-white/10 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-fuchsia-500/50 transition-colors md:col-span-2"
          />
          <input
            type="text"
            placeholder="Unit (e.g. kg)"
            value={unit}
            onChange={(e) => setUnit(e.target.value)}
            required
            className="bg-white/5 border border-white/10 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-fuchsia-500/50 transition-colors"
          />
          <div className="flex gap-2">
            <input
              type="number"
              step="0.1"
              placeholder="Qty"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
              className="bg-white/5 border border-white/10 rounded-lg px-4 py-2 text-sm focus:outline-none focus:border-fuchsia-500/50 transition-colors w-full"
            />
            <button type="submit" className="bg-fuchsia-500/20 text-fuchsia-300 border border-fuchsia-500/50 rounded-lg px-4 hover:bg-fuchsia-500/30 transition-colors font-bold">
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
              <th className="pb-3 font-medium">Stock Level</th>
              {!readOnly && <th className="pb-3 font-medium text-right">Operations</th>}
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.code} className="border-b border-white/5 hover:bg-white/[0.02] transition-colors group">
                <td className="py-3 text-fuchsia-400 font-mono">{item.code}</td>
                <td className="py-3 text-white/90">{item.name}</td>
                <td className="py-3">
                  <div className="flex items-center gap-2">
                    <span className="text-white font-mono text-lg">{item.quantityOnHand}</span>
                    <span className="text-white/40">{item.unit}</span>
                  </div>
                </td>
                {!readOnly && (
                  <td className="py-3 text-right opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => handleAction(item.code, "receive")}
                      className="text-emerald-400 hover:text-emerald-300 text-xs uppercase tracking-wider mr-3 bg-emerald-400/10 px-3 py-1 rounded"
                    >
                      Receive (IN)
                    </button>
                    <button
                      onClick={() => handleAction(item.code, "issue")}
                      className="text-amber-400 hover:text-amber-300 text-xs uppercase tracking-wider bg-amber-400/10 px-3 py-1 rounded"
                    >
                      Issue (OUT)
                    </button>
                  </td>
                )}
              </tr>
            ))}
             {items.length === 0 && !loading && (
              <tr>
                <td colSpan={4} className="py-8 text-center text-white/30 tracking-widest uppercase">
                  No inventory items found
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
