"use client";

import { useEffect, useState } from "react";
import { MarketCard } from "@/components/MarketCard";
import { Card3DModal } from "@/components/Card3DModal";
import { useRouter } from "next/navigation";
import { Loader2 } from "lucide-react";

interface UserProfile {
    id: string;
    username: string;
    email: string;
    balance: number;
    picture: string;
    cards: any[]; // Owned cards
    created: any[]; // Created cards
}

export default function ProfilePage() {
    const router = useRouter();
    const [user, setUser] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [selectedCard, setSelectedCard] = useState<any | null>(null);

    useEffect(() => {
        const token = localStorage.getItem("user_token");

        if (!token) {
            router.push("/");
            return;
        }

        fetch("/api/me", {
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })
            .then((res) => {
                if (!res.ok) throw new Error("Unauthorized");
                return res.json();
            })
            .then((data) => {
                setUser(data);
                setLoading(false);
            })
            .catch(() => {
                localStorage.removeItem("user_token");
                router.push("/");
            });
    }, [router]);

    if (loading) {
        return (
            <div className="flex h-screen items-center justify-center">
                <Loader2 className="animate-spin text-zinc-500" size={32} />
            </div>
        );
    }

    if (!user) return null;

    return (
        <main className="min-h-screen bg-zinc-50 dark:bg-black p-8">
            <div className="max-w-5xl mx-auto">
                {/* Profile Header */}
                <div className="flex flex-col md:flex-row items-start md:items-center gap-6 mb-12 bg-white dark:bg-zinc-900 p-6 rounded-2xl border border-zinc-200 dark:border-zinc-800">
                    <img
                        src={user.picture || "/globe.svg"}
                        alt="Profile"
                        className="w-24 h-24 rounded-full border-4 border-zinc-100 dark:border-zinc-800 object-cover"
                    />
                    <div className="flex-1">
                        <h1 className="text-3xl font-bold tracking-tight">{user.username || "User"}</h1>
                        <p className="text-zinc-500">{user.email}</p>
                    </div>
                    <div className="bg-zinc-100 dark:bg-zinc-800 px-6 py-4 rounded-xl text-center">
                        <p className="text-sm text-zinc-500 uppercase font-medium tracking-wider">Balance</p>
                        <p className="text-2xl font-bold">${user.balance.toFixed(2)}</p>
                    </div>
                </div>

                {/* Owned Cards */}
                <div className="mb-12">
                    <h2 className="text-2xl font-bold mb-6">Your Collection</h2>
                    {user.cards.length === 0 ? (
                        <p className="text-zinc-500">You don't own any cards yet.</p>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                            {user.cards.map(card => (
                                <MarketCard
                                    key={card.id}
                                    card={card}
                                    onClick={() => setSelectedCard(card)}
                                />
                            ))}
                        </div>
                    )}
                </div>

                {/* Created Cards */}
                <div>
                    <h2 className="text-2xl font-bold mb-6">Created Cards</h2>
                    {user.created.length === 0 ? (
                        <p className="text-zinc-500">You haven't created any cards yet.</p>
                    ) : (
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                            {user.created.map(card => (
                                <MarketCard
                                    key={card.id}
                                    card={card}
                                    onClick={() => setSelectedCard(card)}
                                />
                            ))}
                        </div>
                    )}
                </div>
            </div>


            {
                selectedCard && (
                    <Card3DModal
                        card={selectedCard}
                        onClose={() => setSelectedCard(null)}
                    />
                )
            }
        </main >
    );
}
