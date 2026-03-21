import React, {useContext} from 'react'
import {AppContext} from "../context/AppContextProvider.jsx";
import {useUserSync} from "../hooks/useUserSync.js";

function UserSyncHandler() {
    const {beginCreditsLoad, loadUserCredits, stopCreditsLoad} = useContext(AppContext);

    useUserSync({
        onSyncStart: beginCreditsLoad,
        onSynced: loadUserCredits,
        onSyncError: stopCreditsLoad,
    });

    return null;
}

export default UserSyncHandler
