import { NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';
import jwt from 'jsonwebtoken';
import { promises as fs } from 'fs';
import path from 'path';

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

        // Save file to disk instead of base64
        const bytes = await file.arrayBuffer();
        const buffer = Buffer.from(bytes);

        // Generate unique filename
        const timestamp = Date.now();
        const fileExt = file.name.split('.').pop() || 'png';
        const fileName = `card_${timestamp}_${Math.random().toString(36).substring(7)}.${fileExt}`;
        const filePath = path.join(process.cwd(), 'public', 'uploads', fileName);

        // Write file to disk
        await fs.writeFile(filePath, buffer);

        // Store relative URL path (accessible via /uploads/filename)
        const fileUrl = `/uploads/${fileName}`;

        const price = parseFloat(priceStr) || 0.0;
        const copies = parseInt(copiesStr) || 1;
        const isListed = isListedStr === 'true';

        // Get AI Rating
        const aiRating = await getGeminiRating(text);

        const card = await prisma.card.create({
            data: {
                text,
                backgroundUrl: fileUrl,
                price,
                copies,
                isListed: isListed,
                ownerId: userId,
                creatorId: userId,
                aiRating: aiRating
            }
        });

        return NextResponse.json(card);
    } catch (error: any) {
        console.error("Card creation error:", error);
        if (error.code === 'P2002') {
            return NextResponse.json({ error: 'This poem has already been minted!' }, { status: 409 });
        }
        return NextResponse.json({ error: 'Failed to create card' }, { status: 500 });
    }
}

async function getGeminiRating(text: string): Promise<number> {
    try {
        const apiKey = "AIzaSyCk24QOG71kuOcwnXx0VJTMfjNFseIaxWI"; // Hardcoded for now as requested
        const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${apiKey}`;

        const payload = {
            contents: [{
                parts: [{
                    text: `Rate the poetic value and soul of the following text on a strict scale from 1 to 10. 
                    Be critical but fair. Return ONLY the integer number. Nothing else.
                    
                    Text: "${text}"`
                }]
            }]
        };

        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        const ratingText = data.candidates?.[0]?.content?.parts?.[0]?.text;
        if (!ratingText) return 5; // Default average if fails

        const rating = parseInt(ratingText.trim());
        return isNaN(rating) ? 5 : Math.max(1, Math.min(10, rating));

    } catch (error) {
        console.error("Gemini API Error:", error);
        return 0; // 0 indicates failure/unrated
    }
}
