import {useAuth} from "@clerk/clerk-react";
import {useEffect, useState} from "react";
import toast from "react-hot-toast";
import {removeBackgroundImage} from "../services/imageService.js";
import {extractApiErrorMessage} from "../services/apiError.js";

export function useImageUpload({onUploadSuccess}) {
    const {getToken} = useAuth();
    const [status, setStatus] = useState("idle");
    const [uploadProgress, setUploadProgress] = useState(0);
    const [image, setImage] = useState(null);
    const [removedBgImage, setRemovedBgImage] = useState(null);
    const [errorMessage, setErrorMessage] = useState(null);

    useEffect(() => {
        return () => {
            if (removedBgImage) {
                URL.revokeObjectURL(removedBgImage);
            }
        };
    }, [removedBgImage]);

    const clearRemovedImage = () => {
        setRemovedBgImage((currentUrl) => {
            if (currentUrl) {
                URL.revokeObjectURL(currentUrl);
            }

            return null;
        });
    };

    const resetUpload = () => {
        clearRemovedImage();
        setImage(null);
        setErrorMessage(null);
        setStatus("idle");
        setUploadProgress(0);
    };

    const uploadImage = async (file) => {
        if (!file) {
            return;
        }

        clearRemovedImage();
        setImage(file);
        setErrorMessage(null);
        setStatus("uploading");
        setUploadProgress(0);

        try {
            const token = await getToken();
            const imageBlob = await removeBackgroundImage({
                token,
                file,
                onUploadProgress: (progressEvent) => {
                    const progress = progressEvent.total
                        ? Math.round((progressEvent.loaded * 100) / progressEvent.total)
                        : 0;
                    setUploadProgress(progress);
                },
            });

            const removedImageUrl = URL.createObjectURL(imageBlob);
            setRemovedBgImage(removedImageUrl);
            setStatus("success");
            setUploadProgress(100);
            await onUploadSuccess?.();
            toast.success("Image background removed successfully!");
        } catch (error) {
            setStatus("error");
            setUploadProgress(0);
            const message = await extractApiErrorMessage(error, "Failed to upload image. Please try again.");
            setErrorMessage(message);
            toast.error(message);
        }
    };

    const handleImageChange = async (event) => {
        const selectedImage = event.target.files?.[0];
        event.target.value = "";

        if (!selectedImage) {
            return;
        }

        await uploadImage(selectedImage);
    };

    return {
        errorMessage,
        handleImageChange,
        image,
        removedBgImage,
        resetUpload,
        status,
        uploadProgress,
    };
}
