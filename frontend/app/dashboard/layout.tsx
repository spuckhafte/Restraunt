"use client";

import React from "react";
import Link from "next/link";
import { motion } from "framer-motion";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen bg-black/50 backdrop-blur-md text-white relative font-sans w-full">
      {/* Dynamic background gradient */}
      <div className="fixed inset-0 bg-gradient-to-br from-cyan-900/20 via-black/40 to-fuchsia-900/20 pointer-events-none z-0"></div>
      
      {/* Decorative cyber grid */}
      <div className="fixed inset-0 bg-[linear-gradient(rgba(255,255,255,0.02)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.02)_1px,transparent_1px)] bg-[size:50px_50px] pointer-events-none z-0"></div>

      {/* Main Content Area */}
      <div className="relative z-10 min-h-screen flex flex-col">
        <header className="px-8 py-5 border-b border-white/5 bg-black/40 backdrop-blur-xl sticky top-0 z-50">
          <div className="flex items-center justify-between max-w-7xl mx-auto">
            <h1 className="text-xl font-light tracking-widest text-white/70 uppercase">
              <span className="text-cyan-400 font-bold drop-shadow-[0_0_10px_rgba(0,255,255,0.5)]">Restaurant</span>
            </h1>
            <Link 
              href="/"
              className="px-5 py-2 text-xs font-semibold tracking-wider text-white/50 hover:text-cyan-300 hover:bg-cyan-900/20 transition-all border border-transparent hover:border-cyan-400/30 rounded-full"
            >
              TERMINATE SESSION
            </Link>
          </div>
        </header>

        <motion.main 
          initial={{ y: "-100vh", opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ type: "spring", damping: 25, stiffness: 120, duration: 0.8 }}
          className="flex-1 p-6 md:p-8 max-w-7xl mx-auto w-full"
        >
          {children}
        </motion.main>
      </div>
    </div>
  );
}
