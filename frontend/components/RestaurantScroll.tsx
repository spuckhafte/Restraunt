"use client";

import React, { useRef, useEffect, useState } from "react";
import { useScroll, useTransform, motion, MotionValue } from "framer-motion";

const FRAME_COUNT = 25;

interface RestaurantScrollProps {
  externalScrollYProgress?: MotionValue<number>;
}

export default function RestaurantScroll({ externalScrollYProgress }: RestaurantScrollProps = {}) {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  const imagesRef = useRef<(HTMLImageElement | null)[]>(new Array(FRAME_COUNT).fill(null));
  const [isFirstFrameLoaded, setIsFirstFrameLoaded] = useState(false);

  // Preload logic modified for deferred rendering
  useEffect(() => {
    // 1. Load First Frame Critical Path
    const firstImg = new Image();
    firstImg.src = "/restrauntAnimated/ezgif-frame-001.jpg";
    firstImg.onload = () => {
      imagesRef.current[0] = firstImg;
      setIsFirstFrameLoaded(true);

      // 2. Lazy load the rest asynchronously without blocking UI render
      for (let i = 2; i <= FRAME_COUNT; i++) {
        const img = new Image();
        img.src = `/restrauntAnimated/ezgif-frame-${i.toString().padStart(3, "0")}.jpg`;
        img.onload = () => {
          imagesRef.current[i - 1] = img;
        };
      }
    };
  }, []);

  // Use external progress if provided; otherwise initialize our own wrapper target.
  const { scrollYProgress: internalScroll } = useScroll(
    externalScrollYProgress
      ? {}
      : {
        target: containerRef,
        offset: ["start start", "end end"],
      }
  );

  const scrollYProgress = externalScrollYProgress || internalScroll;

  // Calculate current frame
  const currentFrame = useTransform(scrollYProgress, [0, 1], [0, FRAME_COUNT - 1]);

  // Text animations
  const opacity0 = useTransform(scrollYProgress, [0, 0.1, 0.15], [1, 1, 0]);
  const y0 = useTransform(scrollYProgress, [0, 0.1], [0, -50]);

  const opacity30 = useTransform(scrollYProgress, [0.2, 0.3, 0.4, 0.45], [0, 1, 1, 0]);
  const y30 = useTransform(scrollYProgress, [0.2, 0.3], [50, 0]);

  const opacity60 = useTransform(scrollYProgress, [0.5, 0.6, 0.7, 0.75], [0, 1, 1, 0]);
  const y60 = useTransform(scrollYProgress, [0.5, 0.6], [50, 0]);

  const opacity90 = useTransform(scrollYProgress, [0.75, 0.8, 0.85, 0.9], [0, 1, 1, 0]);
  const y90 = useTransform(scrollYProgress, [0.75, 0.8], [50, 0]);

  useEffect(() => {
    if (!isFirstFrameLoaded || !canvasRef.current) return;

    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const render = () => {
      const frameIndex = Math.min(
        FRAME_COUNT - 1,
        Math.max(0, Math.round(currentFrame.get()))
      );

      // Retrieve nearest loaded frame if the target frame isn't ready
      let imgToDraw = imagesRef.current[frameIndex];
      if (!imgToDraw) {
        // Fallback to nearest previous loaded frame
        for (let i = frameIndex - 1; i >= 0; i--) {
          if (imagesRef.current[i]) {
            imgToDraw = imagesRef.current[i];
            break;
          }
        }
      }

      if (imgToDraw && ctx) {
        // Utilize hardware-accelerated CSS scaling by plotting native 1:1 buffers
        if (canvas.width !== imgToDraw.width || canvas.height !== imgToDraw.height) {
          canvas.width = imgToDraw.width;
          canvas.height = imgToDraw.height;
        }

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        ctx.imageSmoothingEnabled = true;
        ctx.imageSmoothingQuality = "high";

        ctx.drawImage(imgToDraw, 0, 0);
      }
    };

    render();

    const unsubscribe = currentFrame.on("change", () => {
      requestAnimationFrame(render);
    });

    const handleResize = () => {
      requestAnimationFrame(render);
    };
    window.addEventListener("resize", handleResize);

    return () => {
      unsubscribe();
      window.removeEventListener("resize", handleResize);
    };
  }, [isFirstFrameLoaded, currentFrame]);

  const StickyContent = (
    <>
      {!isFirstFrameLoaded ? (
        <div className="sticky top-0 h-screen w-full flex items-center justify-center bg-[#050505] z-0">
          <div className="w-8 h-8 border-t-2 border-cyan-400 rounded-full animate-spin"></div>
        </div>
      ) : (
        <div className="sticky top-0 h-screen w-full overflow-hidden flex items-center justify-center z-0">
          <canvas
            ref={canvasRef}
            className="absolute inset-0 w-full h-full object-cover"
          />
        </div>
      )}
    </>
  );

  // If the parent already maps scroll space, just return the sticky payload
  if (externalScrollYProgress) {
    return StickyContent;
  }

  // Otherwise provide the h-[400vh] scrolling sandbox natively
  return (
    <div ref={containerRef} className="relative h-[400vh] bg-[#050505]">
      {StickyContent}
    </div>
  );
}
