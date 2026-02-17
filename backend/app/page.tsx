import { prisma } from "@/lib/prisma";
import { MarketplaceClient } from "@/components/MarketplaceClient";

export const dynamic = 'force-dynamic';

export default async function Home() {
  let cards: any[] = [];
  let error = null;

  try {
    const rawCards = await prisma.card.findMany({
      where: {
        isListed: true,
      },
      include: {
        owner: {
          select: {
            username: true,
          },
        },
      },
      orderBy: {
        createdAt: "desc",
      },
    });

    cards = rawCards.map(card => ({
      ...card,
      createdAt: card.createdAt.toISOString(),
      updatedAt: card.updatedAt.toISOString(),
      // Ensure price is a number
      price: Number(card.price)
    }));
  } catch (e) {
    console.error("Failed to fetch cards:", e);
    error = "Failed to load marketplace. Database may be initializing.";
  }

  return (
    <main className="min-h-screen bg-zinc-50 dark:bg-black p-8">
      <div className="max-w-5xl mx-auto">
        <header className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight mb-2">Marketplace</h1>
          <p className="text-zinc-500 dark:text-zinc-400">
            Discover unique cards created by the community.
          </p>
        </header>

        {error ? (
          <div className="text-center py-20 bg-red-50 dark:bg-red-900/20 rounded-xl border border-dashed border-red-200 dark:border-red-900">
            <p className="text-red-500">{error}</p>
            <p className="text-xs text-red-400 mt-2">Check Vercel Environment Variables & Database Schema.</p>
          </div>
        ) : cards.length === 0 ? (
          <div className="text-center py-20 bg-white dark:bg-zinc-900 rounded-xl border border-dashed border-zinc-200 dark:border-zinc-800">
            <p className="text-zinc-500">No cards listed yet. Be the first!</p>
          </div>
        ) : (
          <MarketplaceClient cards={cards} />
        )}
      </div>
    </main>
  );
}
