import React, {useContext, useEffect} from "react";
import {Link, useSearchParams} from "react-router-dom";
import {AppContext} from "../context/AppContextProvider.jsx";
import {useAuth} from "@clerk/clerk-react";
import CreditsValue from "../components/CreditsValue.jsx";

function PaymentSuccess() {
    const {credits, isCreditsLoading, loadUserCredits} = useContext(AppContext);
    const {isLoaded, isSignedIn} = useAuth();
    const [searchParams] = useSearchParams();
    const sessionId = searchParams.get("session_id");

    useEffect(() => {
        if (isLoaded && isSignedIn) {
            loadUserCredits();
        }
    }, [isLoaded, isSignedIn, loadUserCredits]);

    return (
        <div className="mx-auto flex max-w-3xl flex-col items-center px-4 py-20 text-center font-['Outfit']">
            <div className="rounded-3xl border border-emerald-200 bg-emerald-50 px-8 py-10 shadow-sm">
                <p className="text-sm font-semibold uppercase tracking-[0.24em] text-emerald-600">Payment received</p>
                <h1 className="mt-4 text-3xl font-bold text-gray-900">Your checkout completed successfully.</h1>
                <p className="mt-4 text-base text-gray-600">
                    We are refreshing your credit balance now.
                </p>
                {sessionId && (
                    <p className="mt-4 text-sm text-gray-500">Checkout session: {sessionId}</p>
                )}
                <p className="mt-4 text-sm font-medium text-gray-700">
                    Current credits: <CreditsValue credits={credits} isLoading={isCreditsLoading} />
                </p>
                <div className="mt-8 flex justify-center">
                    <Link
                        to="/"
                        className="rounded-full bg-gray-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-gray-700"
                    >
                        Back to home
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default PaymentSuccess;
