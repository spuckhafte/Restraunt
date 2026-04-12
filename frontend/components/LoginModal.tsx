"use client";

import React, { useState } from "react";
import { motion } from "framer-motion";

export default function LoginModal() {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
      className="fixed inset-0 z-[999] flex items-center justify-center pointer-events-none"
    >
      <div className="relative w-full max-w-md mx-4 pointer-events-auto">
        {/* Neon Gradient Border Wrapper using utility */}
        <div className="neon-border rounded-3xl">
          <div className="glass-panel inner-glow p-8 pt-10 rounded-3xl relative overflow-hidden flex flex-col gap-8 shadow-2xl">
            {/* Top highlight line */}
            <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-cyan-400 to-transparent opacity-50"></div>

            <div className="text-center">
              <h3 className="text-3xl font-light text-white tracking-widest uppercase" style={{ fontFamily: 'var(--font-equinox), sans-serif' }}>
                System Access
              </h3>
              <p className="text-xs text-cyan-200 mt-2 uppercase tracking-widest opacity-60" style={{ fontFamily: 'var(--font-deltha), sans-serif' }}>
                Authenticate to continue
              </p>
            </div>

            <div className="flex flex-col gap-6">
              <div className="relative group">
                <input 
                  type="email" 
                  placeholder="Identification" 
                  className="w-full bg-white/5 border border-white/10 rounded-xl px-5 py-4 text-white placeholder:text-white/30 focus:outline-none focus:border-cyan-400/50 transition-colors peer"
                />
                {/* Simulated Focus Ring */}
                <div className="absolute inset-0 rounded-xl pointer-events-none opacity-0 peer-focus:opacity-100 transition-opacity duration-300 ring-1 ring-cyan-400 shadow-[0_0_15px_rgba(0,255,255,0.4)]"></div>
              </div>

              <div className="relative group">
                <input 
                  type="password" 
                  placeholder="Passcode" 
                  className="w-full bg-white/5 border border-white/10 rounded-xl px-5 py-4 text-white placeholder:text-white/30 focus:outline-none focus:border-fuchsia-500/50 transition-colors peer"
                />
                {/* Simulated Focus Ring */}
                <div className="absolute inset-0 rounded-xl pointer-events-none opacity-0 peer-focus:opacity-100 transition-opacity duration-300 ring-1 ring-fuchsia-500 shadow-[0_0_15px_rgba(255,0,255,0.4)]"></div>
              </div>

              <motion.button 
                whileHover={{ scale: 1.02 }}
                whileTap={{ scale: 0.98 }}
                onHoverStart={() => setIsHovered(true)}
                onHoverEnd={() => setIsHovered(false)}
                className="relative mt-2 w-full bg-white/[0.08] backdrop-blur-md border border-white/20 text-white font-medium py-4 rounded-xl overflow-hidden group transition-colors hover:border-cyan-400/80 hover:bg-white/10 uppercase tracking-widest text-sm"
              >
                <div className="absolute inset-0 bg-gradient-to-r from-fuchsia-600/20 to-cyan-400/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
                <span className="relative z-10 flex items-center justify-center gap-2">
                  Initialize
                  <motion.span 
                    animate={{ x: isHovered ? 4 : 0, opacity: isHovered ? 1 : 0.7 }} 
                    transition={{ duration: 0.2 }}
                  >
                    →
                  </motion.span>
                </span>
              </motion.button>
            </div>
            
            <div className="absolute bottom-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-cyan-500/30 to-transparent"></div>
          </div>
        </div>
      </div>
    </motion.div>
  );
}
