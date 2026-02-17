import { NextRequest, NextResponse } from "next/server";
import { PrismaClient } from "@prisma/client";
import jwt from "jsonwebtoken";

const prisma = new PrismaClient();
const JWT_SECRET = process.env.JWT_SECRET || "super-secret-key-change-me";

export async function POST(req: NextRequest) {
    try {
        const { username } = await req.json();
        const authHeader = req.headers.get("Authorization");

        if (!authHeader || !authHeader.startsWith("Bearer ")) {
            return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
        }

        const token = authHeader.split(" ")[1];
        let decoded: any;
        try {
            decoded = jwt.verify(token, JWT_SECRET);
        } catch (e) {
            return NextResponse.json({ error: "Invalid Token" }, { status: 401 });
        }

        const userId = decoded.userId;

        if (!username || username.trim().length < 3) {
            return NextResponse.json({ error: "Username too short" }, { status: 400 });
        }

        // Check uniqueness
        const existing = await prisma.user.findUnique({
            where: { username }
        });

        if (existing) {
            return NextResponse.json({ error: "Username taken" }, { status: 409 });
        }

        // Update User
        const updatedUser = await prisma.user.update({
            where: { id: userId },
            data: {
                username,
                hasChosenUsername: true
            }
        });

        return NextResponse.json({ success: true, username: updatedUser.username });

    } catch (e) {
        console.error(e);
        return NextResponse.json({ error: "Server Error" }, { status: 500 });
    }
}
