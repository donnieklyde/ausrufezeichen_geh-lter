"use client";

import { useState } from "react";
import { MarketCard } from "@/components/MarketCard";
import { Card3DModal } from "@/components/Card3DModal";

export function MarketplaceClient({ cards }: { cards: any[] }) {
    const [selectedCard, setSelectedCard] = useState<any | null>(null);

    return (
        <>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {cards.map((card) => (
                    <MarketCard
                        key={card.id}
                        card={card}
                        onClick={() => setSelectedCard(card)}
                    />
                ))}
            </div>

            {selectedCard && (
                <Card3DModal
                    card={selectedCard}
                    onClose={() => setSelectedCard(null)}
                />
            )}
        </>
    );
}
