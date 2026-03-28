export const appConfig = {
    backendUrl: import.meta.env.VITE_BACKEND_URL,
    clerkPublishableKey: import.meta.env.VITE_CLERK_PUBLISHABLE_KEY,
    // Pin Clerk JS so production does not silently drift to a newer CDN build.
    clerkJsVersion: import.meta.env.VITE_CLERK_JS_VERSION || "5.110.0",
    stripePublishableKey: import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY,
};

export function requireConfigValue(name, value) {
    if (!value) {
        throw new Error(`Missing ${name}`);
    }

    return value;
}
