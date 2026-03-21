import {apiClient, buildApiUrl, createAuthHeaders} from "./apiClient.js";

export async function removeBackgroundImage({token, file, onUploadProgress}) {
    const formData = new FormData();
    formData.append("image", file);

    const response = await apiClient.post(buildApiUrl("/remove-bg"), formData, {
        headers: createAuthHeaders(token, "Unable to authenticate your upload. Please sign in again."),
        responseType: "blob",
        onUploadProgress,
    });

    return response.data;
}
