"use client";

import React, { useEffect, useRef, useState } from "react";

const FRAME_COUNT = 36;
const ANIMATION_FPS = 25;
const FRAME_DURATION = 1000 / ANIMATION_FPS;

export default function InventoryAnimation({ onComplete }: { onComplete?: () => void }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const imagesRef = useRef<(HTMLImageElement | null)[]>(new Array(FRAME_COUNT).fill(null));
  const isComplete = useRef(false);
  const [isLoaded, setIsLoaded] = useState(false);
  const [useFallback, setUseFallback] = useState(false);

  useEffect(() => {
    let loadedCount = 0;
    let failedCount = 0;

    for (let i = 1; i <= FRAME_COUNT; i++) {
      const img = new Image();
      img.src = `/invAnimated/ezgif-frame-${i.toString().padStart(3, "0")}.jpg`;

      img.onload = () => {
        imagesRef.current[i - 1] = img;
        loadedCount++;
        if (loadedCount + failedCount === FRAME_COUNT) {
          if (failedCount > FRAME_COUNT / 2) setUseFallback(true);
          setIsLoaded(true);
        }
      };

      img.onerror = () => {
        failedCount++;
        if (loadedCount + failedCount === FRAME_COUNT) {
          if (failedCount > FRAME_COUNT / 2) setUseFallback(true);
          setIsLoaded(true);
        }
      };
    }
  }, []);

  useEffect(() => {
    if (!isLoaded || useFallback || !canvasRef.current) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId: number;
    let startTime: number | null = null;

    const renderFrame = (timestamp: number) => {
      if (!startTime) startTime = timestamp;
      const elapsed = timestamp - startTime;

      let currentFrameIndex = Math.floor(elapsed / FRAME_DURATION);

      if (currentFrameIndex >= FRAME_COUNT) {
        currentFrameIndex = FRAME_COUNT - 1;
      }

      let imgToDraw = imagesRef.current[currentFrameIndex];
      if (!imgToDraw) {
        for (let i = currentFrameIndex - 1; i >= 0; i--) {
          if (imagesRef.current[i]) {
            imgToDraw = imagesRef.current[i];
            break;
          }
        }
      }

      if (imgToDraw && ctx) {
        if (canvas.width !== imgToDraw.width || canvas.height !== imgToDraw.height) {
          canvas.width = imgToDraw.width;
          canvas.height = imgToDraw.height;
        }

        ctx.clearRect(0, 0, canvas.width, canvas.height);
        ctx.imageSmoothingEnabled = true;
        ctx.imageSmoothingQuality = "high";
        ctx.drawImage(imgToDraw, 0, 0);
      }

      if (currentFrameIndex < FRAME_COUNT - 1) {
        animationFrameId = requestAnimationFrame(renderFrame);
      } else if (!isComplete.current) {
        isComplete.current = true;
        if (onComplete) onComplete();
      }
    };

    animationFrameId = requestAnimationFrame(renderFrame);

    return () => {
      cancelAnimationFrame(animationFrameId);
    };
  }, [isLoaded, useFallback]);

  return (
    <div className="w-full h-screen bg-black flex items-center justify-center relative">
      {!isLoaded ? (
        <div className="z-10 bg-black/80 backdrop-blur-sm p-6 rounded-2xl border border-white/5 flex flex-col items-center">
          <div className="w-8 h-8 border-t-2 border-cyan-400 border-r-2 border-r-emerald-400 rounded-full animate-spin mb-4"></div>
          <p className="bg-gradient-to-r from-cyan-400 to-emerald-400 bg-clip-text text-transparent font-light tracking-widest uppercase text-xs animate-pulse">
            Approaching Inventory Database...
          </p>
        </div>
      ) : useFallback ? (
        <video
          src="/invAnimated.mp4"
          autoPlay
          muted
          playsInline
          className="absolute inset-0 w-full h-full object-cover mix-blend-screen"
        />
      ) : (
        <canvas ref={canvasRef} className="absolute inset-0 w-full h-full object-cover mix-blend-screen" />
      )}
    </div>
  );
}
