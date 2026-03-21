import {loadStripe} from "@stripe/stripe-js";
import {appConfig, requireConfigValue} from "../config/appConfig.js";
import {apiClient, buildApiUrl, createAuthHeaders} from "./apiClient.js";

let stripePromise;

function getStripeClient() {
    const publishableKey = requireConfigValue("VITE_STRIPE_PUBLISHABLE_KEY", appConfig.stripePublishableKey);

    if (!stripePromise) {
        stripePromise = loadStripe(publishableKey);
    }

    return stripePromise;
}

export async function createCheckoutSession({token, packageId}) {
    const response = await apiClient.post(buildApiUrl("/payments/checkout-sessions"), {
        packageId,
    }, {
        headers: createAuthHeaders(token, "Unable to authenticate your payment request. Please sign in again."),
    });

    return response.data.data;
}

export async function redirectToCheckout(sessionId) {
    const stripe = await getStripeClient();

    if (!stripe) {
        throw new Error("Unable to initialize Stripe checkout.");
    }

    const {error} = await stripe.redirectToCheckout({sessionId});
    if (error) {
        throw error;
    }
}
