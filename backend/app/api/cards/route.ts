import { NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';
import jwt from 'jsonwebtoken';

export const dynamic = 'force-dynamic';

export async function GET(request: Request) {
    try {
        const { searchParams } = new URL(request.url);
        const mode = searchParams.get('mode'); // 'market' or 'my'

        // Authenticate user if possible (required for 'my' mode)
        let userId: string | null = null;
        const authHeader = request.headers.get("Authorization");
        if (authHeader && authHeader.startsWith("Bearer ")) {
            const token = authHeader.split(" ")[1];
            try {
                const decoded = jwt.verify(token, process.env.JWT_SECRET || "super-secret-key-change-me") as { userId: string };
                userId = decoded.userId;
            } catch (e) {
                // Token invalid, ignore for public market view but critical for 'my' view
            }
        }

        let whereClause: any = { isListed: true };

        if (mode === 'my') {
            if (!userId) {
                return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
            }
            whereClause = { ownerId: userId };
        }

        const cards = await prisma.card.findMany({
            where: whereClause,
            orderBy: { createdAt: 'desc' },
            include: {
                owner: {
                    select: {
                        username: true
                    }
                }
            }
        });
        return NextResponse.json(cards);
    } catch (error) {
        console.error(error);
        return NextResponse.json({ error: 'Failed to fetch cards' }, { status: 500 });
    }
}

export async function POST(request: Request) {
    try {
        const authHeader = request.headers.get("Authorization");
        if (!authHeader || !authHeader.startsWith("Bearer ")) {
            return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
        }
        const token = authHeader.split(" ")[1];
        let userId: string;
        try {
            const decoded = jwt.verify(token, process.env.JWT_SECRET || "super-secret-key-change-me") as { userId: string };
            userId = decoded.userId;
        } catch (e) {
            return NextResponse.json({ error: "Invalid token" }, { status: 401 });
        }

        const data = await request.formData();
        const text = data.get('text') as string;
        const priceStr = data.get('price') as string;
        const copiesStr = data.get('copies') as string;
        const isListedStr = data.get('isListed') as string;
        const file = data.get('file') as File;

        if (!file) {
            return NextResponse.json({ error: 'No file uploaded' }, { status: 400 });
        }

        // Vercel Storage Fix: Store as Data URI in the database
        const bytes = await file.arrayBuffer();
        const buffer = Buffer.from(bytes);
        const base64 = buffer.toString('base64');
        const fileUrl = `data:${file.type};base64,${base64}`;

        const price = parseFloat(priceStr) || 0.0;
        const copies = parseInt(copiesStr) || 1;
        const isListed = isListedStr === 'true';

        const card = await prisma.card.create({
            data: {
                text,
                backgroundUrl: fileUrl,
                price,
                copies,
                isListed: isListed,
                ownerId: userId,
                creatorId: userId
            }
        });

        return NextResponse.json(card);
    } catch (error: any) {
        console.error(error);
        if (error.code === 'P2002') {
            return NextResponse.json({ error: 'This poem has already been minted!' }, { status: 409 });
        }
        return NextResponse.json({ error: 'Failed to create card' }, { status: 500 });
    }
}
