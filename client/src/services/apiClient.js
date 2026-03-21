import axios from "axios";
import {appConfig, requireConfigValue} from "../config/appConfig.js";

export const apiClient = axios.create();

export function buildApiUrl(path) {
    const backendUrl = requireConfigValue("VITE_BACKEND_URL", appConfig.backendUrl);
    return `${backendUrl}${path}`;
}

export function createAuthHeaders(token, missingTokenMessage = "Unable to authenticate your request. Please sign in again.") {
    if (!token) {
        throw new Error(missingTokenMessage);
    }

    return {
        Authorization: `Bearer ${token}`,
    };
}
