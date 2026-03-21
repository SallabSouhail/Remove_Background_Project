import React, {useEffect, useState} from 'react'

type ImagePreviewProps = {
    image: File
    onReset: () => void
    removedBgImage: string | null
    status: "idle" | "uploading" | "success" | "error"
    uploadProgress: number
}

function handleClick(blobUrl: string) {
    const link = document.createElement("a");
    link.href = blobUrl;
    link.download = "removed-background.png";
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

function ImagePreview({image, onReset, removedBgImage, status, uploadProgress}: ImagePreviewProps) {
    const [originalPreviewUrl, setOriginalPreviewUrl] = useState("");

    useEffect(() => {
        const objectUrl = URL.createObjectURL(image);
        setOriginalPreviewUrl(objectUrl);

        return () => {
            URL.revokeObjectURL(objectUrl);
        };
    }, [image]);

    return (
        <div className="max-w-6xl mx-auto px-6 py-16">
            <h1 className="text-center text-gray-900 font-bold text-3xl md:text-4xl">
                Remove Image Background
            </h1>
            <p className="text-center text-gray-500 font-semibold mt-4 mb-12">
                Upload an image and download the background-removed result
            </p>

            <div className="grid md:grid-cols-2 gap-10 items-center">
                <div className="bg-white rounded-2xl shadow-xl p-6 text-center">
                    <p className="text-gray-700 font-semibold mb-4">Original Image</p>

                    <div className="border-2 border-dashed border-gray-200 rounded-xl p-4">
                        <img
                            src={originalPreviewUrl}
                            alt="Original"
                            className="rounded-lg mx-auto max-h-[350px] object-contain"
                        />
                    </div>
                </div>

                <div className="bg-white rounded-2xl shadow-xl p-6 text-center">
                    <p className="text-gray-700 font-semibold mb-4">Background Removed</p>

                    <div className="border-2 border-dashed border-gray-200 rounded-xl p-4 bg-gray-50">
                        {status === "uploading" && (
                            <div>
                                <progress value={uploadProgress} max={100} className="my-3 w-full" />
                                <p>{uploadProgress}%</p>
                            </div>
                        )}
                        {removedBgImage != null && (
                            <img
                                src={removedBgImage}
                                alt="Result"
                                className="rounded-lg mx-auto max-h-[350px] object-contain"
                            />
                        )}
                    </div>
                </div>
            </div>

            <div className="flex justify-center gap-6 mt-12">
                <button
                    className="bg-gray-900 text-white px-6 py-3 rounded-full font-medium hover:bg-gray-800 transition cursor-pointer"
                    onClick={onReset}
                >
                    Upload Another
                </button>

                {removedBgImage != null && (
                    <button
                        className="bg-indigo-600 text-white px-6 py-3 rounded-full font-medium hover:bg-indigo-700 transition cursor-pointer"
                        onClick={() => handleClick(removedBgImage)}
                    >
                        Download PNG
                    </button>
                )}
            </div>

        </div>
    )
}

export default ImagePreview
