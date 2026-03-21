import React from "react";
import {Link} from "react-router-dom";

function PaymentCancel() {
    return (
        <div className="mx-auto flex max-w-3xl flex-col items-center px-4 py-20 text-center font-['Outfit']">
            <div className="rounded-3xl border border-amber-200 bg-amber-50 px-8 py-10 shadow-sm">
                <p className="text-sm font-semibold uppercase tracking-[0.24em] text-amber-600">Checkout canceled</p>
                <h1 className="mt-4 text-3xl font-bold text-gray-900">Your purchase was not completed.</h1>
                <p className="mt-4 text-base text-gray-600">
                    No credits were added. You can return to pricing and try again whenever you are ready.
                </p>
                <div className="mt-8 flex justify-center">
                    <Link
                        to="/"
                        className="rounded-full bg-gray-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-gray-700"
                    >
                        Back to pricing
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default PaymentCancel;
