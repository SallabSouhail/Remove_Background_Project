import React, {useContext} from 'react'
import ImagePreview from "./ImagePreview.js";
import {AppContext} from "../context/AppContextProvider.jsx";
import {useImageUpload} from "../hooks/useImageUpload.js";

function ImageUploader() {
    const {loadUserCredits} = useContext(AppContext);
    const {errorMessage, handleImageChange, image, removedBgImage, resetUpload, status, uploadProgress} = useImageUpload({
        onUploadSuccess: loadUserCredits,
    });

    return (
        <>
            <input type="file" id="upload2" accept="image/*" hidden onChange={handleImageChange}/>
            <label id="image-uploader"
                   htmlFor="upload2"
                   className="bg-indigo-600 text-white font-medium rounded-full px-6 py-4
                                        text-lg cursor-pointer inline-block hover:bg-blue-700 transform
                                        transition duration-200 hover:scale-105">
                Upload your image
            </label>
            <p className="text-gray-500 text-sm mt-4">
                or drop a image, paste image or <a href="#" className="text-blue-500 underline">URL</a>
            </p>

            {errorMessage && (
                <div className="mt-4 flex items-start gap-3 rounded-xl bg-red-50 border border-red-200 p-4 shadow-sm">
                    <div className="text-red-600 text-xl">!</div>
                    <div>
                        <p className="text-red-800 font-semibold text-sm">Upload failed</p>
                        <p className="text-red-700 text-sm">{errorMessage}</p>
                    </div>
                </div>
            )}

            {image && (
                <ImagePreview
                    image={image}
                    onReset={resetUpload}
                    removedBgImage={removedBgImage}
                    status={status}
                    uploadProgress={uploadProgress}
                />
            )}
        </>
    )
}

export default ImageUploader
