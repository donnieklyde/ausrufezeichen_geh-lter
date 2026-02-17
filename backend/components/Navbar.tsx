"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { GoogleLogin } from "@react-oauth/google";
import { LayoutGrid, PlusCircle, User, LogOut } from "lucide-react";
import { usePathname } from "next/navigation";
import clsx from "clsx";

export function Navbar() {
    const pathname = usePathname();
    const [sessionToken, setSessionToken] = useState<string | null>(null);

    useEffect(() => {
        const token = localStorage.getItem("user_token");
        setSessionToken(token);
    }, []);

    const handleLoginSuccess = async (credentialResponse: any) => {
        try {
            const { credential } = credentialResponse;
            if (!credential) return;

            const res = await fetch("/api/auth/google", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ idToken: credential }),
            });

            if (!res.ok) throw new Error("Auth failed");

            const data = await res.json();
            localStorage.setItem("user_token", data.token);
            setSessionToken(data.token);

            if (!data.user.hasChosenUsername) {
                window.location.href = "/onboarding";
            } else {
                window.location.reload();
            }
        } catch (e) {
            console.error("Login failed", e);
            alert("Login failed");
        }
    };

    const handleLogout = () => {
        localStorage.removeItem("user_token");
        setSessionToken(null);
        window.location.href = "/";
    };

    const navItems = [
        { name: "Market", href: "/", icon: LayoutGrid },
        { name: "Create", href: "/create", icon: PlusCircle },
        { name: "Profile", href: "/profile", icon: User },
    ];

    return (
        <nav className="border-b border-zinc-200 dark:border-zinc-800 bg-white dark:bg-black p-4">
            <div className="max-w-5xl mx-auto flex items-center justify-between">
                <Link href="/" className="text-xl font-bold tracking-tighter">
                    slikkroad
                </Link>

                {sessionToken ? (
                    <div className="flex items-center gap-6">
                        {navItems.map((item) => {
                            const Icon = item.icon;
                            const isActive = pathname === item.href;
                            return (
                                <Link
                                    key={item.href}
                                    href={item.href}
                                    className={clsx(
                                        "flex items-center gap-2 text-sm font-medium transition-colors",
                                        isActive
                                            ? "text-black dark:text-white"
                                            : "text-zinc-500 hover:text-black dark:text-zinc-400 dark:hover:text-white"
                                    )}
                                >
                                    <Icon size={18} />
                                    <span>{item.name}</span>
                                </Link>
                            );
                        })}
                        <button
                            onClick={handleLogout}
                            className="ml-4 text-zinc-500 hover:text-red-500 transition-colors"
                        >
                            <LogOut size={18} />
                        </button>
                    </div>
                ) : (
                    <GoogleLogin
                        onSuccess={handleLoginSuccess}
                        onError={() => console.log("Login Failed")}
                        useOneTap
                        theme="filled_black"
                        shape="pill"
                    />
                )}
            </div>
        </nav>
    );
}
