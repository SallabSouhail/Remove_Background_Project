import {useState} from "react";
import {useAuth, useClerk} from "@clerk/clerk-react";
import toast from "react-hot-toast";
import {createCheckoutSession, redirectToCheckout} from "../services/paymentService.js";
import {extractApiErrorMessage} from "../services/apiError.js";

export function useCheckout() {
    const {isLoaded, isSignedIn, getToken} = useAuth();
    const {openSignIn} = useClerk();
    const [loadingPlanId, setLoadingPlanId] = useState(null);
    const [errorMessage, setErrorMessage] = useState("");

    const startCheckout = async (packageId) => {
        if (!isLoaded) {
            return;
        }

        if (!isSignedIn) {
            openSignIn({});
            return;
        }

        setErrorMessage("");
        setLoadingPlanId(packageId);

        try {
            const token = await getToken();
            const {sessionId} = await createCheckoutSession({token, packageId});
            await redirectToCheckout(sessionId);
        } catch (error) {
            const message = await extractApiErrorMessage(error, "Unable to start checkout right now.");
            setErrorMessage(message);
            toast.error(message);
        } finally {
            setLoadingPlanId(null);
        }
    };

    return {
        errorMessage,
        loadingPlanId,
        startCheckout,
    };
}
