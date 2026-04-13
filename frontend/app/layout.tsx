import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Restaurant OMS Frontend",
  description: "Staff dashboard for menu, sales, inventory, reports, manager override, and supplier checks.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${inter.variable} h-full antialiased bg-[#050505] text-white`}
    >
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
