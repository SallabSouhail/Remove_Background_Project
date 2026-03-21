import React from 'react'
import {plans} from '../assets/assets.js'
import {useCheckout} from "../hooks/useCheckout.js";

function Pricing() {
    const {errorMessage, loadingPlanId, startCheckout} = useCheckout();

    return (
        <div className="py-10 md:px-20">
            <div className="container mx-auto px-4">
                <div className="mb-12 text-center">
                    <h2 className="font-bold text-3xl md:text-4xl text-gray-900 mb-12">Choose your perfect package</h2>
                    <p className="mt-4 text-black-400 max-w-2xl mx-auto">
                        Select from our carefully curated photography packages designed to meet your specific needs and budget.
                    </p>
                </div>
                {errorMessage && (
                    <div className="mx-auto mb-6 max-w-2xl rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {errorMessage}
                    </div>
                )}

                <div className="grid gap-8 md:grid-cols-2 lg:grid-cols-3">
                    {plans.map((plan) => (
                        <div
                            key={plan.id}
                            className={`relative pt-6 px-6 ${plan.popular ? "backdrop-blur-lg rounded-2xl" : "border-gray-800 rounded-xl"} 
                                        bg-[#1A1A1A] transform hover:-translate-y-2 transition-all duration-300`}
                        >
                            {plan.popular && (
                                <div className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 rounded-full bg-purple-600 px-3 py-1 text-white text-sm font-semibold">
                                    Most Popular
                                </div>
                            )}

                            <div className="text-center p-6 font-bold">
                                <h3 className="text-2xl text-white">{plan.name}</h3>
                                <div className="mt-4">
                                  <span className="text-4xl text-violet-400">
                                    ${plan.price}
                                  </span>
                                </div>
                            </div>

                            <div className="px-4 pb-8">
                                <ul className="mb-8 space-y-4">
                                    <li className="flex items-center text-white">
                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            strokeWidth={2}
                                            stroke="currentColor"
                                            className="w-5 h-5 text-purple-400 mr-2"
                                        >
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                        </svg>
                                        {plan.credits}
                                    </li>
                                    <li className="flex items-center text-white">
                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            fill="none"
                                            viewBox="0 0 24 24"
                                            strokeWidth={2}
                                            stroke="currentColor"
                                            className="w-5 h-5 text-purple-400 mr-2"
                                        >
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                                        </svg>
                                        {plan.description}
                                    </li>
                                </ul>

                                <button
                                    type="button"
                                    onClick={() => startCheckout(plan.id)}
                                    disabled={loadingPlanId !== null}
                                    className="w-full py-3 px-6 text-center text-white font-semibold rounded-full
                                              bg-gradient-to-r from-purple-500 to-indigo-500 shadow-lg hover:from-purple-600 hover:to-indigo-600
                                              transition duration-300 ease-in-out transform hover:scale-105 cursor-pointer
                                              disabled:cursor-not-allowed disabled:opacity-70 disabled:hover:scale-100"
                                >
                                    {loadingPlanId === plan.id ? "Redirecting..." : "Choose plan"}
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default Pricing
