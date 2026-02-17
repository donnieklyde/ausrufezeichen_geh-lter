import { NextRequest, NextResponse } from "next/server";
import { OAuth2Client } from "google-auth-library";
import jwt from "jsonwebtoken";
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);
const JWT_SECRET = process.env.JWT_SECRET || "super-secret-key-change-me";

export async function POST(req: NextRequest) {
    try {
        const { idToken } = await req.json();

        if (!idToken) {
            return NextResponse.json({ error: "Missing ID Token" }, { status: 400 });
        }

        // Verify Google Token
        const ticket = await client.verifyIdToken({
            idToken: idToken,
            audience: process.env.GOOGLE_CLIENT_ID, // Specify the CLIENT_ID of the app that accesses the backend
        });

        const payload = ticket.getPayload();
        if (!payload) {
            return NextResponse.json({ error: "Invalid Token Payload" }, { status: 401 });
        }

        const { sub: googleId, email, name, picture } = payload;

        if (!email) {
            return NextResponse.json({ error: "Email not provided by Google" }, { status: 400 });
        }

        // Find or Create User
        let user = await prisma.user.findUnique({
            where: { googleId },
        });

        if (!user) {
            // Check if email exists (maybe from other provider later?)
            user = await prisma.user.findUnique({
                where: { email }
            });

            if (user) {
                // Link Google ID
                const updateData: any = { googleId, picture };
                if (!user.username) {
                    updateData.username = name || email.split("@")[0];
                }
                user = await prisma.user.update({
                    where: { email },
                    data: updateData
                });
            } else {
                // Create new user
                // Generate a username from email if needed, or use name
                // Handle unique constraint on username if we keep it unique
                let username = name || email.split("@")[0];

                // Simple uniqueness check/retry loop could be added here
                // For now, let's append random string if needed? 
                // Prisma throws if unique constraint failed.
                // Let's TRY to create.

                try {
                    user = await prisma.user.create({
                        data: {
                            email,
                            googleId,
                            username,
                            picture
                        }
                    });
                } catch (e) {
                    // Fallback username
                    username = `${username}_${Math.floor(Math.random() * 1000)}`;
                    user = await prisma.user.create({
                        data: {
                            email,
                            googleId,
                            username,
                            picture
                        }
                    });
                }
            }
        }

        // Create Session Token (JWT)
        const token = jwt.sign(
            { userId: user.id, email: user.email },
            JWT_SECRET,
            { expiresIn: "30d" }
        );

        return NextResponse.json({
            token,
            user: {
                id: user.id,
                username: user.username,
                email: user.email,
                picture: user.picture,
                balance: user.balance,
                hasChosenUsername: user.hasChosenUsername
            }
        });

    } catch (error) {
        console.error("Auth Error:", error);
        return NextResponse.json({ error: "Authentication Failed" }, { status: 500 });
    }
}
