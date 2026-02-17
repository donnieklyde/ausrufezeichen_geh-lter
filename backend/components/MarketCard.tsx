import Image from "next/image";

interface CardProps {
    id: string;
    text: string;
    backgroundUrl: string;
    price: number;
    aiRating: number;
    owner: {
        username: string | null;
    } | null;
}

export function MarketCard({ card, onClick }: { card: CardProps; onClick?: () => void }) {
    // Fix background URL for local display if needed
    // If it's a relative path starting with /uploads, it should work fine
    const bgUrl = card.backgroundUrl.startsWith("http")
        ? card.backgroundUrl
        : card.backgroundUrl;

    return (
        <div
            onClick={onClick}
            className="relative aspect-[0.7] w-full overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800 shadow-sm transition-all hover:scale-[1.02] hover:shadow-md group cursor-pointer"
        >
            {/* Background Image */}
            <img
                src={bgUrl}
                alt={card.text}
                className="h-full w-full object-cover"
            />

            {/* Overlay */}
            <div className="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors" />

            {/* HDMI/Reflective Shine (optional, kept subtle) */}

            {/* Footer Info */}
            <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/80 to-transparent p-4 text-white">
                <div className="flex items-center justify-between">
                    <span className="text-xs font-medium opacity-80">
                        @{card.owner?.username || "Unknown"}
                    </span>
                    <div className="flex items-center gap-2">
                        {card.aiRating > 0 && (
                            <span className="text-xs font-bold text-purple-400 flex items-center gap-1">
                                ✨ {card.aiRating}/10
                            </span>
                        )}
                        <span className="font-bold text-amber-400">
                            ${Number(card.price).toFixed(2)}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
}
