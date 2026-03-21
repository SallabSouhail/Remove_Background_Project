import React from 'react'
import ImageUploader from "./ImageUploader.tsx";
function TryNow() {
    return (
        <div>
            <h1 className="text-center text-gray-900 font-bold text-3xl md:text-4xl">Remove Image Background</h1>
            <p className="text-center text-gray-500 font-semibold my-8">Get a transparent background for any image.</p>
            <div className="flex justify-center content-center items-center">
                <div className="rounded-2xl shadow-xl p-10 text-center">
                    <ImageUploader/>
                </div>
            </div>
        </div>
    )
}

export default TryNow
