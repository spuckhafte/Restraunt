"use client";

import React, { useRef, useState } from "react";
import { useScroll, motion } from "framer-motion";
import { useRouter } from "next/navigation";
import dynamic from "next/dynamic";
import LoginForm from "./LoginForm";
import AdminAnimation from "./AdminAnimation";
import ManagerAnimation from "./ManagerAnimation";

// Dashboard Imports
import DashboardLayout from "@/app/dashboard/layout";
import AdminDashboard from "@/app/dashboard/admin/page";
import ManagerDashboard from "@/app/dashboard/manager/page";
import ClerkDashboard from "@/app/dashboard/clerk/page";
import InventoryDashboard from "@/app/dashboard/inventory/page";

const ClerkAnimation = dynamic(() => import("./ClerkAnimation"), {
  ssr: false,
});

const InventoryAnimation = dynamic(() => import("./InventoryAnimation"), {
  ssr: false,
});

const DynamicRestaurantScroll = dynamic(() => import("./RestaurantScroll"), {
  ssr: false,
});

export default function LoginFlow() {
  const containerRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({
    target: containerRef,
    offset: ["start start", "end end"],
  });

  const router = useRouter();

  const [role, setRole] = useState<string | null>(null);
  const [showFlash, setShowFlash] = useState<string | null>(null);
  const [showDashboardOverlay, setShowDashboardOverlay] = useState(false);

  // When login is successful
  const handleLoginSuccess = (userRole: string) => {
    if (userRole === "admin" || userRole === "manager" || userRole === "clerk" || userRole === "inventory") {
      setShowFlash(userRole); // Initialize distinct flash logic natively
      
      // We securely hide the login modal *AFTER* the flash opacity hits 100% to ensure no flicker!
      setTimeout(() => {
        setRole(userRole);
        setTimeout(() => setShowFlash(null), 200); // Wait additional 200ms to fade out tail
      }, 200);
    } else {
      setRole(userRole);
    }
  };

  if (role === "admin") {
    return (
      <main className="w-full h-screen bg-black overflow-hidden relative">
        <AdminAnimation onComplete={() => setShowDashboardOverlay(true)} />
        
        {/* Flash finishing tail */}
        {showFlash && (
          <motion.div 
            initial={{ opacity: 1 }}
            animate={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-[1000] bg-white mix-blend-screen pointer-events-none"
          />
        )}

        {/* Top-to-Bottom Dashboard Slide-in Overlay */}
        {showDashboardOverlay && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.7 }}
            className="absolute inset-0 z-[2000] overflow-y-auto overflow-x-hidden"
          >
            <DashboardLayout>
              <AdminDashboard />
            </DashboardLayout>
          </motion.div>
        )}
      </main>
    );
  }

  if (role === "manager") {
    return (
      <main className="w-full h-screen bg-black overflow-hidden relative">
        <ManagerAnimation onComplete={() => setShowDashboardOverlay(true)} />
        
        {/* Flash finishing tail for Manager */}
        {showFlash && (
          <motion.div 
            initial={{ opacity: 1 }}
            animate={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-[1000] bg-cyan-100 mix-blend-screen pointer-events-none"
          />
        )}

        {/* Top-to-Bottom Dashboard Slide-in Overlay */}
        {showDashboardOverlay && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.7 }}
            className="absolute inset-0 z-[2000] overflow-y-auto overflow-x-hidden"
          >
            <DashboardLayout>
              <ManagerDashboard />
            </DashboardLayout>
          </motion.div>
        )}
      </main>
    );
  }

  if (role === "clerk") {
    return (
      <main className="w-full h-screen bg-black overflow-hidden relative">
        <ClerkAnimation onComplete={() => setShowDashboardOverlay(true)} />
        
        {/* Flash finishing tail for Clerk */}
        {showFlash && (
          <motion.div 
            initial={{ opacity: 1 }}
            animate={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-[1000] bg-gradient-to-tr from-fuchsia-200 to-cyan-200 mix-blend-screen pointer-events-none"
          />
        )}

        {/* Top-to-Bottom Dashboard Slide-in Overlay */}
        {showDashboardOverlay && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.7 }}
            className="absolute inset-0 z-[2000] overflow-y-auto overflow-x-hidden"
          >
            <DashboardLayout>
              <ClerkDashboard />
            </DashboardLayout>
          </motion.div>
        )}
      </main>
    );
  }

  if (role === "inventory") {
    return (
      <main className="w-full h-screen bg-black overflow-hidden relative">
        <InventoryAnimation onComplete={() => setShowDashboardOverlay(true)} />
        
        {/* Flash finishing tail for Inventory */}
        {showFlash && (
          <motion.div 
            initial={{ opacity: 1 }}
            animate={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-[1000] bg-gradient-to-tr from-cyan-200 to-emerald-200 mix-blend-screen pointer-events-none"
          />
        )}

        {/* Top-to-Bottom Dashboard Slide-in Overlay */}
        {showDashboardOverlay && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.7 }}
            className="absolute inset-0 z-[2000] overflow-y-auto overflow-x-hidden"
          >
            <DashboardLayout>
              <InventoryDashboard />
            </DashboardLayout>
          </motion.div>
        )}
      </main>
    );
  }

  if (role) {
    return (
      <main className="w-full h-screen flex flex-col items-center justify-center bg-black text-white relative">
        <div className="absolute inset-0 bg-gradient-to-t from-cyan-900/20 to-transparent"></div>
        <div className="text-center z-10 glass-panel p-10 rounded-3xl border border-white/10">
          <h1 className="text-4xl font-light text-cyan-400 tracking-widest uppercase mb-4 shadow-cyan-400/50 drop-shadow-[0_0_15px_rgba(0,255,255,0.4)]">
            ACCESS GRANTED
          </h1>
          <p className="text-lg text-white/50 tracking-widest uppercase">
            Protocol: {role}
          </p>
          <div className="mt-8 px-6 py-2 border border-cyan-400/50 rounded-full text-cyan-400/80 text-sm tracking-widest animate-pulse">
            System Online
          </div>
        </div>
      </main>
    );
  }

  return (
    <div ref={containerRef} className="relative h-[400vh] bg-[#050505]">
      {/* Scrollable Context (The Restaurant Background) */}
      <DynamicRestaurantScroll externalScrollYProgress={scrollYProgress} />

      {/* Cyberpunk Flash Overlay for Admin Transition */}
      {showFlash === "admin" && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1.1 }}
          transition={{ duration: 0.2, ease: "easeOut" }}
          className="fixed inset-0 z-[1000] bg-white mix-blend-screen pointer-events-none"
        />
      )}

      {/* Cyberpunk Flash Overlay for Manager Transition */}
      {showFlash === "manager" && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.98 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.2, ease: "easeOut" }}
          className="fixed inset-0 z-[1000] bg-gradient-to-t from-cyan-400 via-white to-cyan-100 mix-blend-screen pointer-events-none"
        />
      )}

      {/* Cyberpunk Flash Overlay for Clerk Transition */}
      {showFlash === "clerk" && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.98 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.2, ease: "easeOut" }}
          className="fixed inset-0 z-[1000] bg-gradient-to-tr from-fuchsia-500 via-white to-cyan-400 mix-blend-screen pointer-events-none"
        />
      )}

      {/* Cyberpunk Flash Overlay for Inventory Transition */}
      {showFlash === "inventory" && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.98 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.2, ease: "easeOut" }}
          className="fixed inset-0 z-[1000] bg-gradient-to-tr from-cyan-500 via-white to-emerald-400 mix-blend-screen pointer-events-none"
        />
      )}

      {/* Decoupled LoginForm fixed at z-999 mapped to scroll completion */}
      <LoginForm 
        scrollYProgress={scrollYProgress} 
        onSuccess={handleLoginSuccess}
      />
    </div>
  );
}
