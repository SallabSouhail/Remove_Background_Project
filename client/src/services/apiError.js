import axios from "axios";

export async function extractApiErrorMessage(error, fallbackMessage) {
    if (axios.isAxiosError(error)) {
        const errorData = error.response?.data;

        if (errorData instanceof Blob) {
            try {
                const text = await errorData.text();
                const parsed = JSON.parse(text);
                if (typeof parsed?.data === "string" && parsed.data.trim()) {
                    return parsed.data;
                }
            } catch {
                return fallbackMessage;
            }
        }

        if (typeof errorData === "string" && errorData.trim()) {
            return errorData;
        }

        if (typeof errorData === "object" && typeof errorData?.data === "string" && errorData.data.trim()) {
            return errorData.data;
        }
    }

    if (error instanceof Error && error.message) {
        return error.message;
    }

    return fallbackMessage;
}
