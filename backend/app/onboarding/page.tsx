"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Loader2, ArrowRight } from "lucide-react";

export default function OnboardingPage() {
    const router = useRouter();
    const [username, setUsername] = useState("");
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setIsLoading(true);

        try {
            const token = localStorage.getItem("user_token");
            if (!token) {
                router.push("/");
                return;
            }

            const res = await fetch("/api/user/username", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },
                body: JSON.stringify({ username })
            });

            const data = await res.json();

            if (!res.ok) {
                throw new Error(data.error || "Failed to set username");
            }

            // Update local token or state if needed? 
            // Ideally we re-fetch user profile, or just proceed.
            // Let's assume the session token is still valid (it uses ID, not username usually in payload if strict).
            // Actually our JWT payload has { userId, email }. So username change doesn't invalidate it.

            router.push("/create");
            router.refresh();

        } catch (err: any) {
            setError(err.message);
            setIsLoading(false);
        }
    };

    return (
        <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 dark:bg-black p-4 font-[Bahnschrift]">
            <div className="w-full max-w-md space-y-8 bg-white dark:bg-zinc-900 p-8 rounded-2xl border border-zinc-200 dark:border-zinc-800 shadow-xl">
                <div className="text-center">
                    <h1 className="text-3xl font-bold tracking-tight">Welcome, Poet.</h1>
                    <p className="mt-2 text-zinc-500 dark:text-zinc-400">
                        Choose a unique name for your gallery.
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label htmlFor="username" className="sr-only">Username</label>
                        <input
                            id="username"
                            name="username"
                            type="text"
                            required
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="w-full rounded-xl border border-zinc-300 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-950 px-4 py-3 placeholder-zinc-400 focus:border-black dark:focus:border-white focus:outline-none focus:ring-1 focus:ring-black dark:focus:ring-white transition-all font-[Bahnschrift] text-lg"
                            placeholder="username"
                        />
                        {error && (
                            <p className="mt-2 text-sm text-red-500 font-bold animate-pulse">
                                {error}
                            </p>
                        )}
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading || username.length < 3}
                        className="flex w-full items-center justify-center gap-2 rounded-xl bg-black dark:bg-white px-4 py-3 text-white dark:text-black font-bold hover:opacity-90 disabled:opacity-50 transition-all font-[Bahnschrift]"
                    >
                        {isLoading ? (
                            <Loader2 className="animate-spin" size={20} />
                        ) : (
                            <>
                                <span>Start Creating</span>
                                <ArrowRight size={20} />
                            </>
                        )}
                    </button>
                </form>
            </div>
        </main>
    );
}
