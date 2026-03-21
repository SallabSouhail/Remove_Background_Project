import React, {useContext} from 'react'
import {assets} from "../assets/assets.js"
import {Menu, X} from "lucide-react";
import {Link} from "react-router-dom";
import {SignedIn, SignedOut, useClerk, UserButton, useUser} from "@clerk/clerk-react";
import {AppContext} from "../context/AppContextProvider.jsx";
import CreditsValue from "./CreditsValue.jsx";

function Menubar() {
    const [menuOpen, setMenuOpen] = React.useState(false);
    const {openSignIn, openSignUp} = useClerk();
    const {user} = useUser();
    const {credits, isCreditsLoading} = useContext(AppContext);

    const openRegister = () => {
        setMenuOpen(false);
        openSignUp({});
    }

    const openLogin = () => {
        setMenuOpen(false);
        openSignIn({});
    }

    return (
        <nav className="flex justify-between items-center bg-white px-8 py-4 relative">
            <div className="flex items-center space-x-2 cursor-pointer">
                <Link to={'/'} className="bg-white px-8 py-4 flex justify-between items-center space-x-2 cursor-pointer">
                    <img src={assets.logo} alt="logo" className="w-8 h-8 object-contain" />
                    <span className="text-2xl font-semibold text-indigo-700 ">
                    remove.
                    <span className="text-gray-400">bg</span>
                </span>
                </Link>
            </div>

            <div className="hidden md:flex items-center space-x-4">
                <SignedOut>
                    <button className="text-gray-700 font-medium cursor-pointer hover:text-blue-500 transition duration-200" onClick={openLogin}>Log in</button>
                    <button className="bg-gray-100 text-gray-700 font-medium rounded-full px-4 py-2 cursor-pointer hover:bg-gray-200 transition duration-200" onClick={openRegister}>Sign Up</button>
                </SignedOut>
                <SignedIn>
                    <div className="flex items-center gap-2 sm:gap-3">
                        <button className="flex items-center gap-2 bg-blue-100 px-4 sm:px-5 py-1 sm:py-2 rounded-full hover:scale-105 transition-all duration-200 ">
                            <img src={assets.credit} alt="credits" height={24} width={24}/>
                            <p className="text-xs sm:text-sm font-medium text-gray-600">
                                Credits: <CreditsValue credits={credits} isLoading={isCreditsLoading} />
                            </p>
                        </button>
                        <p className="text-gray-600 max-sm:hidden">
                            Hi, {user?.fullName}
                        </p>
                    </div>
                    <UserButton />
                </SignedIn>
            </div>

            <div className="flex md:hidden" >
                <button className="cursor-pointer transition duration-200" onClick={() => setMenuOpen(!menuOpen)}>
                    {menuOpen ? <X size={28} /> : <Menu size={28}/>}
                </button>
                {menuOpen && (
                    <div className="bg-white shadow-md p-4 absolute top-16 right-8 rounded-md flex flex-col justify-center items-center space-y-4 w-40 ">
                        <SignedOut>
                            <button className="text-gray-700 hover:text-blue-500 font-medium cursor-pointer" onClick={openLogin}>
                                Log in
                            </button>
                            <button className="bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium px-4 py-2 rounded-full text-center cursor-pointer w-full"
                                    onClick={openRegister}
                            >
                                Sign up
                            </button>
                        </SignedOut>
                        <SignedIn>
                            <div className="flex items-center gap-2 sm:gap-3">
                                <button className="flex items-center gap-1 bg-blue-100 px-2 py-1 rounded-full hover:scale-105 transition-all duration-200 ">
                                    <img src={assets.credit} alt="credits" height={24} width={24}/>
                                    <p className="text-xs sm:text-sm font-medium text-gray-600">
                                        Credits: <CreditsValue credits={credits} isLoading={isCreditsLoading} />
                                    </p>
                                </button>
                            </div>
                            <UserButton />
                        </SignedIn>
                    </div>
                )}
            </div>
        </nav>
    )
}

export default Menubar
