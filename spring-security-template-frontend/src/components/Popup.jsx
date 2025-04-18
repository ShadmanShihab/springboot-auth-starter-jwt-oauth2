import React from 'react';

const Popup = ({ message, onClose }) => {
    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
            <div className="bg-white p-8 rounded-lg shadow-xl max-w-lg w-full"> {/* Increased padding and max-width */}
                <p className="py-6 text-lg">{message}</p> {/* Increased padding and font size */}
                <div className="flex justify-center mt-6"> {/* Added container for centered button */}
                    <button className="btn primary outline" onClick={onClose}>Close</button>
                </div>
            </div>
        </div>
    );
};

export default Popup;