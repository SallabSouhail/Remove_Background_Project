import {useAuth, useUser} from "@clerk/clerk-react";
import {useEffect, useRef} from "react";
import toast from "react-hot-toast";
import {extractApiErrorMessage} from "../services/apiError.js";
import {syncUser} from "../services/userService.js";

export function useUserSync({onSyncStart, onSynced, onSyncError}) {
    const {isLoaded, isSignedIn, getToken} = useAuth();
    const {user} = useUser();
    const syncedUserIdRef = useRef(null);
    const syncInFlightRef = useRef(false);

    useEffect(() => {
        if (!isLoaded || !isSignedIn || !user?.id) {
            return;
        }

        if (syncedUserIdRef.current === user.id || syncInFlightRef.current) {
            return;
        }

        let isMounted = true;
        syncInFlightRef.current = true;
        onSyncStart?.();

        const saveUser = async () => {
            try {
                const token = await getToken();
                const savedUser = await syncUser({token, user});
                syncedUserIdRef.current = user.id;

                if (savedUser?.isCreated) {
                    toast.success("User successfully created!");
                }

                if (isMounted) {
                    await onSynced?.();
                }
            } catch (error) {
                if (isMounted) {
                    onSyncError?.();
                    const message = await extractApiErrorMessage(error, "User sync failed. Please try again later.");
                    toast.error(message);
                }
            } finally {
                syncInFlightRef.current = false;
            }
        };

        saveUser();

        return () => {
            isMounted = false;
        };
    }, [getToken, isLoaded, isSignedIn, onSyncError, onSyncStart, onSynced, user]);
}
