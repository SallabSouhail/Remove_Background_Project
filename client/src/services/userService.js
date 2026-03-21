import {apiClient, buildApiUrl, createAuthHeaders} from "./apiClient.js";

export async function syncUser({token, user}) {
    const email = user?.primaryEmailAddress?.emailAddress;
    if (!email) {
        throw new Error("Unable to sync your account because your primary email is missing.");
    }

    const response = await apiClient.post(buildApiUrl("/users"), {
        clerkId: user.id,
        email,
        firstName: user.firstName,
        lastName: user.lastName,
        photoUrl: user.imageUrl,
    }, {
        headers: createAuthHeaders(token, "Unable to authenticate your account sync. Please sign in again."),
    });

    return response.data.data;
}

export async function fetchUserCredits({token}) {
    const response = await apiClient.get(buildApiUrl("/users/credits"), {
        headers: createAuthHeaders(token, "Unable to authenticate your credit request. Please sign in again."),
    });

    return response.data.data?.credits ?? 0;
}
