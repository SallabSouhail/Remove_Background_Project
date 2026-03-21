import {createContext, useCallback, useEffect, useState} from "react";
import {useAuth} from "@clerk/clerk-react";
import toast from "react-hot-toast";
import {fetchUserCredits} from "../services/userService.js";
import {extractApiErrorMessage} from "../services/apiError.js";

export const AppContext = createContext();

const AppContextProvider = ({children}) => {
    const [credits, setCredits] = useState(null);
    const [isCreditsLoading, setIsCreditsLoading] = useState(false);
    const {getToken, isLoaded, isSignedIn} = useAuth();

    useEffect(() => {
        if (!isLoaded) {
            return;
        }

        if (!isSignedIn) {
            setCredits(null);
            setIsCreditsLoading(false);
            return;
        }

        setCredits(null);
        setIsCreditsLoading(true);
    }, [isLoaded, isSignedIn]);

    const beginCreditsLoad = useCallback(() => {
        setIsCreditsLoading(true);
    }, []);

    const stopCreditsLoad = useCallback(() => {
        setIsCreditsLoading(false);
    }, []);

    const loadUserCredits = useCallback(async () => {
        if (!isLoaded || !isSignedIn) {
            setCredits(null);
            setIsCreditsLoading(false);
            return;
        }

        setIsCreditsLoading(true);

        try {
            const token = await getToken();
            const nextCredits = await fetchUserCredits({token});
            setCredits(nextCredits);
        } catch (error) {
            const message = await extractApiErrorMessage(error, "Failed to load user credits. Please try again later.");
            toast.error(message);
        } finally {
            setIsCreditsLoading(false);
        }
    }, [getToken, isLoaded, isSignedIn]);

    const contextValue = {
        beginCreditsLoad,
        credits,
        isCreditsLoading,
        loadUserCredits,
        stopCreditsLoad,
    };

    return (
        <AppContext value={contextValue}>
            {children}
        </AppContext>
    );
};

export default AppContextProvider;
