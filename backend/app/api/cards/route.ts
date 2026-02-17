import { NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';
import { writeFile, mkdir } from 'fs/promises';
import path from 'path';

export const dynamic = 'force-dynamic';

export async function GET(request: Request) {
    try {
        const { searchParams } = new URL(request.url);
        const mode = searchParams.get('mode'); // 'market' or 'my'
        const userId = "demo-user-id";

        let whereClause: any = { isListed: true };

        if (mode === 'my') {
            whereClause = { ownerId: userId };
        }

        const cards = await prisma.card.findMany({
            where: whereClause,
            orderBy: { createdAt: 'desc' },
            include: { owner: true }
        });
        return NextResponse.json(cards);
    } catch (error) {
        return NextResponse.json({ error: 'Failed to fetch cards' }, { status: 500 });
    }
}

export async function POST(request: Request) {
    try {
        const data = await request.formData();
        const text = data.get('text') as string;
        const priceStr = data.get('price') as string;
        const isListedStr = data.get('isListed') as string;
        const file = data.get('file') as File;
        const ownerId = "demo-user-id";

        if (!file) {
            return NextResponse.json({ error: 'No file uploaded' }, { status: 400 });
        }

        // Save file
        const bytes = await file.arrayBuffer();
        const buffer = Buffer.from(bytes);

        // Ensure upload dir exists
        const uploadDir = path.join(process.cwd(), 'public', 'uploads');
        await mkdir(uploadDir, { recursive: true });

        const filename = `${Date.now()}-${file.name.replace(/\s/g, '_')}`;
        const filepath = path.join(uploadDir, filename);
        await writeFile(filepath, buffer);

        const fileUrl = `/uploads/${filename}`;
        const price = parseFloat(priceStr) || 0.0;
        const isListed = isListedStr === 'true';

        // Create Card
        // Ensure User exists
        let user = await prisma.user.findUnique({ where: { username: "demo_user" } });
        if (!user) {
            user = await prisma.user.create({
                data: { username: "demo_user", id: ownerId }
            });
        }

        const card = await prisma.card.create({
            data: {
                text,
                backgroundUrl: fileUrl,
                price,
                isListed: isListed,
                ownerId: user.id,
                creatorId: user.id
            }
        });

        return NextResponse.json(card);
    } catch (error) {
        console.error(error);
        return NextResponse.json({ error: 'Failed to create card' }, { status: 500 });
    }
}
