"use client";

import { GoogleOAuthProvider } from "@react-oauth/google";
import { ReactNode } from "react";

export function Providers({ children }: { children: ReactNode }) {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;

    // Fallback for build time if missing (prevents crash, but login won't work without real ID)
    const effectiveClientId = clientId || "dummy-client-id-for-build";

    return (
        <GoogleOAuthProvider clientId={effectiveClientId}>
            {children}
        </GoogleOAuthProvider>
    );
}
