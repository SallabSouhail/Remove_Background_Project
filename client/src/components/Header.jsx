import React from 'react'
import {assets} from "../assets/assets.js";

function Header() {
    function handleClick() {
       /* e.preventDefault();*/
        const element = document.getElementById("image-uploader");
        element?.scrollIntoView({ behavior: "smooth" });
    }

    return (
        <header className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center mb-16">
            {/* Left Side: video banner*/}
            <div className="order-last md:order-first flex justify-center">
                <div className="shadow-[0_25px_50px_-12px_rgba(0,0,0,0.15)] rounded-4xl overflow-hidden">
                    <video src = {assets.video_banner} autoPlay loop muted className="w-full max-w-[400px] h-auto object-cover"></video>
                </div>
            </div>
            {/*Right side: Text content*/}
            <div >
                <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-6 leading-tight mx-auto w-fit md:mx-0">
                    The fastest
                    <span className="text-indigo-700"> background eraser</span>
                </h1>
                <p className="text-gray-600 mb-8 text-lg leading-relaxed">
                    Transform your photos with our background remover app!
                    Highlight your subject and create a transparent background,
                    so you can place it in a variety of new designs and destinations.
                    Try it now and immerse your subject in a completely different environment!
                </p>

                <div className= "w-fit mx-auto md:mx-0">
                    <label
                           className="bg-black text-white font-medium rounded-full px-8 py-4
                                       text-lg cursor-pointer inline-block hover:opacity-80 transform
                                       transition duration-200 hover:scale-105"
                           onClick={handleClick}
                    >
                        Try It Now
                    </label>
                </div>

            </div>

        </header>
    )
}

export default Header

