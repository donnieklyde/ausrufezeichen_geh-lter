import { NextRequest, NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';

export const dynamic = 'force-dynamic';

interface Context {
    params: Promise<{ id: string }>;
}

export async function PATCH(
    request: NextRequest,
    context: Context
) {
    try {
        const { id } = await context.params;
        const body = await request.json();
        const { isListed, price } = body;

        const data: any = {};
        if (typeof isListed === 'boolean') data.isListed = isListed;
        if (typeof price === 'number') data.price = price;

        const card = await prisma.card.update({
            where: { id },
            data,
        });

        return NextResponse.json(card);
    } catch (error) {
        return NextResponse.json({ error: 'Failed to update card' }, { status: 500 });
    }
}

export async function GET(
    request: NextRequest,
    context: Context
) {
    try {
        const { id } = await context.params;
        const card = await prisma.card.findUnique({
            where: { id },
            include: { owner: true, creator: true }
        });

        if (!card) {
            return NextResponse.json({ error: "Card not found" }, { status: 404 });
        }

        return NextResponse.json(card);
    } catch (e) {
        return NextResponse.json({ error: "Failed to fetch card" }, { status: 500 });
    }
}
