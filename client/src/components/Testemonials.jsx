import React from 'react'
import {testimonials} from "../assets/assets.js";

function Testemonials() {
    return (
        <div className="max-w-7xl px-4 mx-auto sm:px-6 lg:px-8 py-12 " >
            <h2 className="text-3xl md:text-4xl font-bold text-center">
                They love us. You will too!
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 px-6 my-8 ">
                {testimonials.map(testimonial=>(
                    <div key={testimonial.id} className="flex flex-col justify-between max-w-md mx-auto md:mx-0 shadow-xl  rounded-xl hover:shadow-lg transition-shadow ">
                        <div className="mb-10 px-6 pt-7 ">
                            <svg width="25px" height="25px" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M5.29289 1.29291L6.70711 2.70712L3 6.41423V7.00001H7V14H1V5.5858L5.29289 1.29291Z" fill="#000000"/>
                                <path d="M15 7.00001H11V6.41423L14.7071 2.70712L13.2929 1.29291L9 5.5858V14H15V7.00001Z" fill="#000000"/>
                            </svg>

                            <p className="mt-4 text-gray-700" style={{hyphens:"auto"}}>{testimonial.quote}</p>
                        </div>

                        <div className="bg-gray-50 px-6 py-5  rounded-b-xl">
                            <h4 className="font-semibold text-gray-900">{testimonial.author}</h4>
                            <span className="text-sm text-gray-500">{testimonial.handle}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default Testemonials
