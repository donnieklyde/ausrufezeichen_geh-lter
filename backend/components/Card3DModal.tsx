"use client";

import { useEffect, useRef, useState } from "react";
import { X } from "lucide-react";

interface Card3DModalProps {
    card: any;
    onClose: () => void;
}

export function Card3DModal({ card, onClose }: Card3DModalProps) {
    const cardRef = useRef<HTMLDivElement>(null);
    const [rotate, setRotate] = useState({ x: 0, y: 0 });
    const [shine, setShine] = useState({ x: 0, y: 0, opacity: 0 });

    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            if (!cardRef.current) return;

            const rect = cardRef.current.getBoundingClientRect();
            const centerX = rect.left + rect.width / 2;
            const centerY = rect.top + rect.height / 2;

            const mouseX = e.clientX;
            const mouseY = e.clientY;

            // Calculate rotation (max 15 degrees)
            const rotateX = ((mouseY - centerY) / (window.innerHeight / 2)) * -15;
            const rotateY = ((mouseX - centerX) / (window.innerWidth / 2)) * 15;

            setRotate({ x: rotateX, y: rotateY });

            // Calculate shine position (relative to card)
            const shineX = ((mouseX - rect.left) / rect.width) * 100;
            const shineY = ((mouseY - rect.top) / rect.height) * 100;

            setShine({ x: shineX, y: shineY, opacity: 1 });
        };

        const handleMouseLeave = () => {
            setRotate({ x: 0, y: 0 });
            setShine({ ...shine, opacity: 0 });
        };

        window.addEventListener("mousemove", handleMouseMove);
        // window.removeEventListener("mouseleave", handleMouseLeave); // Global listen, so maybe reset on close?

        return () => {
            window.removeEventListener("mousemove", handleMouseMove);
        };
    }, []);

    // Prevent click propigation to backdrop
    const handleCardClick = (e: React.MouseEvent) => {
        e.stopPropagation();
    };

    const bgUrl = card.backgroundUrl.startsWith("http")
        ? card.backgroundUrl
        : card.backgroundUrl;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-sm p-4 animate-in fade-in duration-200"
            onClick={onClose}
        >
            <button
                onClick={onClose}
                className="absolute top-4 right-4 text-white/50 hover:text-white transition-colors"
            >
                <X size={32} />
            </button>

            <div
                ref={cardRef}
                onClick={handleCardClick}
                className="relative w-full max-w-sm aspect-[0.7] rounded-xl shadow-2xl transition-transform duration-100 ease-out"
                style={{
                    perspective: "1000px",
                    transform: `perspective(1000px) rotateX(${rotate.x}deg) rotateY(${rotate.y}deg) scale(1.1)`,
                    transformStyle: "preserve-3d",
                }}
            >
                {/* Shine Effect */}
                <div
                    className="absolute inset-0 z-20 pointer-events-none rounded-xl mix-blend-overlay transition-opacity duration-300"
                    style={{
                        background: `radial-gradient(circle at ${shine.x}% ${shine.y}%, rgba(255,255,255,0.8) 0%, rgba(255,255,255,0) 80%)`,
                        opacity: shine.opacity
                    }}
                />

                {/* Card Content (Cloned from MarketCard but stripped of hover/footer interactions logic for pure display) */}
                <div className="absolute inset-0 rounded-xl overflow-hidden bg-zinc-900 border border-zinc-800">
                    <img
                        src={bgUrl}
                        alt="Card Background"
                        className="h-full w-full object-cover"
                    />
                    <div className="absolute inset-0 bg-black/20" />
                    <div className="absolute inset-0 flex items-center justify-center p-6">
                        <p className="text-center font-bold text-white whitespace-pre-wrap" style={{ fontSize: 'clamp(1rem, 5vw, 2.5rem)', lineHeight: 1.2 }}>
                            {card.text}
                        </p>
                    </div>
                </div>

                {/* Optional: Add thickness/depth layers if we want to get fancy, but single layer is fine for now */}
            </div>

            {/* Spotlight on the floor/backdrop? */}
            <div className="absolute inset-0 pointer-events-none bg-radial-gradient from-white/10 to-transparent opacity-50" style={{ zIndex: -1 }} />
        </div>
    );
}
