import React, {useState} from 'react'
import {assets, categories} from "../assets/assets.js";
function BgSlider() {
    const [sliderPosition, setSliderPosition ] = useState(50);
    const [activeCategory, setActiveCategory] = useState("People");

    const handleSliderChange = (e) => {
        setSliderPosition(e.target.value);
    }

    const getImages = () => {
        switch (activeCategory) {
            case "People":
                return {
                    original: assets.people_org,
                    edited: assets.people_edited
                };
            case "Products":
                return {
                    original: assets.product_org,
                    edited: assets.product_edited
                };
            case "Animals":
                return {
                    original: assets.animals_org,
                    edited: assets.animals_edited
                };
            case "Cars":
            return {
                original: assets.cars_org,
                edited: assets.cars_edited
            };
            default:
                return {
                    original: assets.people_org,
                    edited: assets.people_edited
                };
        }
    }
    return (
        <div className="mb-16">
            {/* Section title*/}
            <h2 className="font-bold text-center text-3xl md:text-4xl text-gray-900 mb-12">
                Stunning Quality
            </h2>

            {/* Category Selector*/}
            <div className="flex justify-center mb-10 items-center ">
                <div className="inline-flex flex-wrap justify-center gap-4 space-x-6 mb-8 bg-gray-100 px-6 py-3 rounded-full shadow-sm font-semibold">
                    {categories.map((category, index) => (
                        <button key={index}
                                onClick={()=>setActiveCategory(category)}
                                className=
                                    {`px-6 py-2 rounded-full font-medium cursor-pointer ${activeCategory === category?
                                    'bg-white text-gray-800 shadow-sm' : 'text-gray-600 hover:bg-gray-200'}`}
                        >
                            {category}
                        </button>
                    ))}
                </div>
            </div>

            {/* Image Slider*/}
            <div className="relative w-full max-w-4xl m-auto overflow-hidden rounded-xl shadow-xl">
                <img src={getImages().original}
                     alt="original image"
                     style={{clipPath: `inset(0 ${100.2 - sliderPosition}% 0 0)`}}
                />
                <img src={getImages().edited}
                     alt="Background removed image"
                     style={{clipPath: `inset(0 0 0 ${sliderPosition}%)`}}
                     className="absolute top-0 left-0 w-full h-full"
                />
                <input type="range"
                       min={0}
                       max={100}
                       value={sliderPosition}
                       onChange={handleSliderChange}
                       className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-full z-10 slider"
                />
            </div>

        </div>
    )
}

export default BgSlider
