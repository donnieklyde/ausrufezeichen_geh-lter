import Image from "next/image";

interface CardProps {
    id: string;
    text: string;
    backgroundUrl: string;
    price: number;
    owner: {
        username: string | null;
    } | null;
}

export function MarketCard({ card }: { card: CardProps }) {
    // Fix background URL for local display if needed
    // If it's a relative path starting with /uploads, it should work fine
    const bgUrl = card.backgroundUrl.startsWith("http")
        ? card.backgroundUrl
        : card.backgroundUrl;

    return (
        <div className="relative aspect-[0.7] w-full overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm transition-all hover:scale-[1.02] hover:shadow-md group">
            {/* Background Image */}
            <img
                src={bgUrl}
                alt="Card Background"
                className="h-full w-full object-cover"
            />

            {/* Overlay */}
            <div className="absolute inset-0 bg-black/20 group-hover:bg-black/10 transition-colors" />

            {/* Text Content */}
            <div className="absolute inset-0 flex items-center justify-center p-6">
                <p className="text-center font-bold text-white drop-shadow-[0_2px_4px_rgba(0,0,0,0.8)] whitespace-pre-wrap" style={{ fontSize: 'clamp(1rem, 5vw, 2rem)', lineHeight: 1.2 }}>
                    {card.text}
                </p>
            </div>

            {/* Footer Info */}
            <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-4 text-white">
                <div className="flex items-center justify-between">
                    <span className="text-xs font-medium opacity-80">
                        @{card.owner?.username || "Unknown"}
                    </span>
                    <span className="font-bold text-amber-400">
                        ${card.price.toFixed(2)}
                    </span>
                </div>
            </div>
        </div>
    );
}
