"use client";

import { useState, useRef, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Loader2, Upload, Save, LayoutGrid, Dice5 } from "lucide-react";

type EffectType = 'none' | 'vintage' | 'noir' | 'chromatic' | 'spectral' | 'filmburn';

export default function CreatePage() {
    const router = useRouter();
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const imgRef = useRef<HTMLImageElement | null>(null); // Store loaded image

    const [text, setText] = useState("Your\nPoetic\nText");
    const [price, setPrice] = useState("0.00");
    const [image, setImage] = useState<File | null>(null);
    const [imagePreview, setImagePreview] = useState<string | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [activeEffect, setActiveEffect] = useState<EffectType>('none');
    const [effectSeed, setEffectSeed] = useState<number>(1);
    const [imageLoaded, setImageLoaded] = useState(false); // Track load state

    // Load Image when preview changes
    useEffect(() => {
        if (imagePreview) {
            const img = new Image();
            img.src = imagePreview;
            img.onload = () => {
                imgRef.current = img;
                setImageLoaded(true);
            };
        } else {
            imgRef.current = null;
            setImageLoaded(false);
        }
    }, [imagePreview]);

    // Re-draw when dependencies change
    useEffect(() => {
        drawCanvas(true);
    }, [text, imageLoaded, activeEffect, effectSeed]);

    const handleEffectClick = (effect: EffectType) => {
        setActiveEffect(effect);
        setEffectSeed(Math.random());
    };

    const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files[0]) {
            const file = e.target.files[0];
            setImage(file);
            const reader = new FileReader();
            reader.onload = (ev) => {
                setImagePreview(ev.target?.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const drawCanvas = (includeText: boolean = false) => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;

        // Set canvas dimensions
        canvas.width = 1080;
        canvas.height = 1540; // 0.7 aspect ratio

        // 1. Fill Background
        ctx.fillStyle = "#0a0a0a";
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        if (imgRef.current) {
            const img = imgRef.current;

            // Draw Image with specific effects using filter
            ctx.save();
            applyEffectToContext(ctx, canvas, activeEffect, effectSeed);

            // Draw image covering the canvas
            const scale = Math.max(canvas.width / img.width, canvas.height / img.height);
            const x = (canvas.width / 2) - (img.width / 2) * scale;
            const y = (canvas.height / 2) - (img.height / 2) * scale;
            ctx.drawImage(img, x, y, img.width * scale, img.height * scale);

            ctx.restore();

            // 2. Post-processing Effects (Overlays)
            applyOverlayEffects(ctx, canvas, activeEffect, effectSeed);

            // 3. Draw Text (Only if requested)
            if (includeText) {
                drawText(ctx, canvas);
            }
        } else {
            // No image active
            if (includeText) {
                drawText(ctx, canvas);
            }
        }
    };

    const applyEffectToContext = (ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement, effect: EffectType, seed: number) => {
        const r = (val: number, range: number) => val + (seed - 0.5) * range;

        switch (effect) {
            case 'vintage':
                ctx.filter = `sepia(${r(0.6, 0.2)}) contrast(${r(1.2, 0.3)}) brightness(${r(0.9, 0.2)})`;
                break;
            case 'noir':
                ctx.filter = `grayscale(${r(1, 0)}) contrast(${r(1.5, 0.5)}) brightness(${r(0.8, 0.2)})`;
                break;
            case 'spectral':
                ctx.filter = `hue-rotate(${r(90, 180)}deg) contrast(${r(1.1, 0.2)}) saturate(${r(1.5, 0.5)})`;
                break;
            case 'filmburn':
                ctx.filter = `contrast(${r(1.2, 0.3)}) saturate(${r(1.3, 0.4)}) sepia(${r(0.3, 0.2)})`;
                break;
            default:
                ctx.filter = "none";
        }
    };

    const applyOverlayEffects = (ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement, effect: EffectType, seed: number) => {
        ctx.save();

        const r = (val: number, range: number) => val + (seed - 0.5) * range;

        switch (effect) {
            case 'chromatic':
                const offset = 5 + (seed * 10);
                ctx.globalCompositeOperation = "screen";
                ctx.fillStyle = `rgba(255, 0, 0, ${0.1 + seed * 0.1})`;
                ctx.translate(offset, 0);
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                ctx.translate(-offset * 2, 0);
                ctx.fillStyle = `rgba(0, 0, 255, ${0.1 + seed * 0.1})`;
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                break;

            case 'vintage':
                addNoise(ctx, canvas, 0.05 + (seed * 0.05));
                const grad = ctx.createRadialGradient(canvas.width / 2, canvas.height / 2, canvas.width / 3, canvas.width / 2, canvas.height / 2, canvas.width);
                grad.addColorStop(0, "transparent");
                grad.addColorStop(1, "rgba(0,0,0,0.6)");
                ctx.fillStyle = grad;
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                break;

            case 'filmburn':
                const burnX = seed * 400;
                const burnGrad = ctx.createLinearGradient(burnX, 0, burnX + 200, canvas.height);
                burnGrad.addColorStop(0, `rgba(255, ${100 * seed}, 0, 0.4)`);
                burnGrad.addColorStop(1, "transparent");
                ctx.globalCompositeOperation = "screen";
                ctx.fillStyle = burnGrad;
                ctx.fillRect(0, 0, canvas.width, canvas.height);
                if (seed > 0.5) {
                    ctx.fillStyle = "rgba(255, 200, 150, 0.1)";
                    ctx.fillRect(canvas.width - 200, 0, 200, canvas.height);
                }
                break;
        }

        ctx.restore();
    };

    const addNoise = (ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement, amount: number) => {
        const w = canvas.width, h = canvas.height;
        ctx.save();
        ctx.globalCompositeOperation = "overlay";
        ctx.fillStyle = `rgba(255, 255, 255, ${amount})`;
        ctx.fillRect(0, 0, w, h);
        ctx.restore();
    };

    const drawText = (ctx: CanvasRenderingContext2D, canvas: HTMLCanvasElement) => {
        ctx.fillStyle = "rgba(0, 0, 0, 0.3)";
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillStyle = "white";

        const lines = text.split('\n');
        let fontSize = 140;
        let lineHeight = fontSize * 1.2;

        const padding = 100;
        const maxWidth = canvas.width - (padding * 2);
        const maxHeight = canvas.height - (padding * 2);

        let fits = false;
        while (!fits && fontSize > 20) {
            ctx.font = `bold ${fontSize}px Bahnschrift, sans-serif`;
            const totalHeight = lines.length * (fontSize * 1.2);
            const widestLine = Math.max(...lines.map(line => ctx.measureText(line).width));

            if (totalHeight < maxHeight && widestLine < maxWidth) {
                fits = true;
            } else {
                fontSize -= 5;
            }
        }

        ctx.font = `bold ${fontSize}px Bahnschrift, sans-serif`;
        lineHeight = fontSize * 1.2;

        ctx.shadowColor = "rgba(0, 0, 0, 0.8)";
        ctx.shadowBlur = 30;
        ctx.shadowOffsetX = 0;
        ctx.shadowOffsetY = 15;

        const totalBlockHeight = lines.length * lineHeight;
        let startY = (canvas.height - totalBlockHeight) / 2 + (lineHeight / 2);

        lines.forEach((line, i) => {
            ctx.fillText(line, canvas.width / 2, startY + (i * lineHeight));
            ctx.strokeStyle = "rgba(0,0,0,0.5)";
            ctx.lineWidth = fontSize / 30;
            ctx.strokeText(line, canvas.width / 2, startY + (i * lineHeight));
        });
    };

    const handleSave = async (isListed: boolean) => {
        if (!canvasRef.current || isProcessing) return;
        setIsProcessing(true);

        try {
            const token = localStorage.getItem("user_token");
            if (!token) {
                alert("Please login first");
                router.push("/");
                return;
            }

            // 1. Draw with text synchronously (since image is preloaded)
            drawCanvas(true);

            // 2. Allow a tiny tick for canvas buffer update (sometimes needed in browser)
            // but generally synchronous. 
            setTimeout(() => {
                canvasRef.current!.toBlob(async (blob) => {
                    if (!blob) return;

                    const formData = new FormData();
                    formData.append("text", text);
                    formData.append("price", price);
                    formData.append("isListed", isListed.toString());
                    const filename = image ? image.name : "created_card.png";
                    formData.append("file", blob, filename);

                    const res = await fetch("/api/cards", {
                        method: "POST",
                        headers: { Authorization: `Bearer ${token}` },
                        body: formData
                    });

                    if (!res.ok) throw new Error("Failed to create card");

                    // 3. Restore Preview (No Text)
                    drawCanvas(false);
                    router.push("/profile");
                }, "image/png");
            }, 10);

        } catch (e) {
            console.error(e);
            alert("Failed to save card");
            setIsProcessing(false);
            drawCanvas(false);
        }
    };

    return (
        <main className="min-h-screen bg-zinc-50 dark:bg-black p-8 font-[Bahnschrift]">
            <div className="max-w-6xl mx-auto grid grid-cols-1 lg:grid-cols-2 gap-12">

                {/* Editor Controls */}
                <div className="space-y-8 order-2 lg:order-1">
                    <div>
                        <h1 className="text-3xl font-bold tracking-tight mb-2">Create Card</h1>
                        <p className="text-zinc-500">Design your poetic card.</p>
                    </div>

                    <div className="space-y-6 bg-white dark:bg-zinc-900 p-6 rounded-2xl border border-zinc-200 dark:border-zinc-800">

                        {/* Text Input */}
                        <div>
                            <label className="block text-sm font-medium mb-2">Card Text</label>
                            <textarea
                                value={text}
                                onChange={(e) => setText(e.target.value)}
                                className="w-full p-3 rounded-xl bg-zinc-100 dark:bg-zinc-800 border-none resize-none focus:ring-2 ring-black dark:ring-white font-[Bahnschrift] text-sm"
                                rows={5}
                                placeholder="Type your poem here..."
                            />
                            <p className="text-xs text-zinc-500 mt-2">Newlines will be preserved.</p>
                        </div>

                        {/* Effect Selector */}
                        <div>
                            <label className="block text-sm font-medium mb-2 flex items-center gap-2">
                                Visual Effect
                                <Dice5 size={14} className="text-zinc-400" />
                            </label>
                            <div className="grid grid-cols-3 gap-2">
                                {(['none', 'vintage', 'noir', 'chromatic', 'spectral', 'filmburn'] as EffectType[]).map((effect) => (
                                    <button
                                        key={effect}
                                        onClick={() => handleEffectClick(effect)}
                                        className={`px-3 py-2 rounded-lg text-sm font-medium capitalize transition border ${activeEffect === effect
                                            ? 'bg-black text-white dark:bg-white dark:text-black border-transparent'
                                            : 'bg-zinc-100 dark:bg-zinc-800 text-zinc-600 dark:text-zinc-400 border-transparent hover:bg-zinc-200 dark:hover:bg-zinc-700'
                                            }`}
                                    >
                                        {effect}
                                    </button>
                                ))}
                            </div>
                            <p className="text-xs text-zinc-500 mt-2">Click again to randomize parameters.</p>
                        </div>

                        {/* Image Upload */}
                        <div>
                            <label className="block text-sm font-medium mb-2">Background Image</label>
                            <div className="flex items-center gap-4">
                                <label className="flex items-center justify-center gap-2 px-4 py-2 bg-zinc-100 dark:bg-zinc-800 rounded-lg cursor-pointer hover:bg-zinc-200 dark:hover:bg-zinc-700 transition">
                                    <Upload size={18} />
                                    <span>Upload</span>
                                    <input type="file" accept="image/*" onChange={handleImageUpload} className="hidden" />
                                </label>
                                {image && <span className="text-sm text-zinc-500 truncate">{image.name}</span>}
                            </div>
                        </div>

                        {/* Price */}
                        <div>
                            <label className="block text-sm font-medium mb-2">Price ($)</label>
                            <input
                                type="number"
                                value={price}
                                onChange={(e) => setPrice(e.target.value)}
                                className="w-full p-3 rounded-xl bg-zinc-100 dark:bg-zinc-800 border-none focus:ring-2 ring-black dark:ring-white font-[Bahnschrift]"
                                step="0.01"
                                min="0"
                            />
                        </div>
                    </div>

                    <div className="flex gap-4">
                        <button
                            onClick={() => handleSave(false)}
                            disabled={isProcessing}
                            className="flex-1 flex items-center justify-center gap-2 py-4 bg-zinc-200 dark:bg-zinc-800 rounded-xl font-bold hover:bg-zinc-300 dark:hover:bg-zinc-700 transition disabled:opacity-50"
                        >
                            {isProcessing ? <Loader2 className="animate-spin" /> : <Save size={20} />}
                            <span>Save Draft</span>
                        </button>
                        <button
                            onClick={() => handleSave(true)}
                            disabled={isProcessing}
                            className="flex-1 flex items-center justify-center gap-2 py-4 bg-black dark:bg-white text-white dark:text-black rounded-xl font-bold hover:opacity-90 transition disabled:opacity-50"
                        >
                            {isProcessing ? <Loader2 className="animate-spin" /> : <LayoutGrid size={20} />}
                            <span>List on Market</span>
                        </button>
                    </div>
                </div>

                {/* Live Preview */}
                <div className="order-1 lg:order-2 flex flex-col items-center lg:sticky lg:top-8 gap-4">
                    <div className="relative w-full max-w-[400px] aspect-[0.7] rounded-2xl overflow-hidden shadow-2xl border border-zinc-200 dark:border-zinc-800 bg-zinc-900">
                        <canvas
                            ref={canvasRef}
                            className="w-full h-full object-cover"
                        />
                    </div>
                    <p className="text-sm text-zinc-400">Preview (Text hidden, applied on save)</p>
                </div>

            </div>
        </main>
    );
}
