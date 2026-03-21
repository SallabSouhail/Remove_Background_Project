import React from 'react'
import {assets} from "../assets/assets.js";
import {FOOTER_CONSTANTS} from "../assets/assets.js";
function Footer() {
    return (
        <footer className="flex justify-center items-center py-5 bg-gray-50">
            <img src={assets.logo} className="w-8 mr-4" alt="logo"/>
            <p>
                &copy; {new Date().getFullYear()} created by <a href="#" className="text-blue-500 underline">@Sallab Souhail</a> | All rights reserved.
            </p>
            <div className="flex justify-center items-center gap-3 ml-8 md:ml-15">
                {FOOTER_CONSTANTS.map((item, index) => (
                    <a key={index} href={item.url} target="_blank" rel="noopener noreferrer">
                        <img src={item.logo} alt="logo" width={32}/>
                    </a>
                ))}
            </div>
        </footer>
    )
}

export default Footer
