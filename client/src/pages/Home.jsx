import React from 'react'
import Header from "../components/Header.jsx";
import BgRemovalSteps from "../components/BgRemovalSteps.jsx";
import BgSlider from "../components/BgSlider.jsx";
import Pricing from "../components/Pricing.jsx";
import Testemonials from "../components/Testemonials.jsx";
import TryNow from "../components/TryNow.jsx";

function Home() {
    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 font-['Outfit']">
            <Header />
            <BgRemovalSteps/>
            <BgSlider />
            <Pricing/>
            <Testemonials/>
            <TryNow/>
        </div>
    )
}

export default Home
