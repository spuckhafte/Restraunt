"use client";

import React, { useEffect, useRef, useState } from "react";

const FRAME_COUNT = 35;
const ANIMATION_FPS = 30;
const FRAME_DURATION = 1000 / ANIMATION_FPS;

export default function AdminAnimation({ onComplete }: { onComplete?: () => void }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const imagesRef = useRef<(HTMLImageElement | null)[]>(new Array(FRAME_COUNT).fill(null));
  const isComplete = useRef(false);
  const [isLoaded, setIsLoaded] = useState(false);

  useEffect(() => {
    let loadedCount = 0;

    for (let i = 1; i <= FRAME_COUNT; i++) {
      const img = new Image();
      img.src = `/adminAnimated/ezgif-frame-${i.toString().padStart(3, "0")}.jpg`;

      img.onload = () => {
        imagesRef.current[i - 1] = img;
        loadedCount++;
        if (loadedCount === FRAME_COUNT) {
          setIsLoaded(true);
        }
      };

      // Fallback for missing assets ensuring we do not indefinitely hang
      img.onerror = () => {
        imagesRef.current[i - 1] = i > 1 ? imagesRef.current[i - 2] : null;
        loadedCount++;
        if (loadedCount === FRAME_COUNT) setIsLoaded(true);
      };
    }
  }, []);

  useEffect(() => {
    if (!isLoaded || !canvasRef.current) return;

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

      // Read fallback if actual frame represents null hole
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
        // Use native image resolution for the canvas buffer
        if (canvas.width !== imgToDraw.width || canvas.height !== imgToDraw.height) {
          canvas.width = imgToDraw.width;
          canvas.height = imgToDraw.height;
        }

        // Draw natively 1:1
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Enhance internal image smoothing for any fractional buffers
        ctx.imageSmoothingEnabled = true;
        ctx.imageSmoothingQuality = "high";

        ctx.drawImage(imgToDraw, 0, 0);
      }

      // Loop only if we have not reached the end
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
  }, [isLoaded]);

  return (
    <div className="w-full h-screen bg-black flex items-center justify-center relative">
      {!isLoaded ? (
        <div className="z-10 bg-black/80 backdrop-blur-sm p-6 rounded-2xl border border-white/5 flex flex-col items-center">
          <div className="w-8 h-8 border-t-2 border-magenta-500 rounded-full animate-spin mb-4"></div>
          <p className="text-fuchsia-400 font-light tracking-widest uppercase text-xs animate-pulse">
            Accessing Administrative Deep Archives...
          </p>
        </div>
      ) : (
        <canvas ref={canvasRef} className="absolute inset-0 w-full h-full object-cover mix-blend-screen" />
      )}
    </div>
  );
}
