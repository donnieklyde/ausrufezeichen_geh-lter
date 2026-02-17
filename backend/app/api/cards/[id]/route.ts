import { NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';

export const dynamic = 'force-dynamic';

export async function PATCH(
    request: Request,
    { params }: { params: { id: string } }
) {
    try {
        const id = params.id;
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
